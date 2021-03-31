package com.doit.dataflowtemplates;

import com.google.api.client.googleapis.util.Utils;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TypeDescriptor;

/**
 * The {@code BigQueryToPubSub} pipeline publishes records to Cloud Pub/Sub from a Big Query Table.
 *
 * <p>Example Usage:
 *
 * <pre>
 * {@code ./gradlew clean shadowJar && \
 * java -jar build/libs/dataflowtemplates-1.0-SNAPSHOT.jar
 * --project=${PROJECT_ID} \
 * --region=${REGION} \
 * --stagingLocation=gs://${PROJECT_ID}/dataflow/pipelines/${PIPELINE_FOLDER}/staging \
 * --tempLocation=gs://${PROJECT_ID}/dataflow/pipelines/${PIPELINE_FOLDER}/temp \
 * --runner=DataflowRunner \
 * --bigQueryTableName=project:dataset.table \
 * --outputTopic=projects/${PROJECT_ID}/topics/${TOPIC_NAME}
 * }
 * </pre>
 */
public class BigQueryToPubSub {
  /**
   * Main entry-point for the pipeline. Reads in the command-line arguments, parses them, and
   * executes the pipeline.
   *
   * @param args Arguments passed in from the command-line.
   */
  public static void main(String[] args) {
    final var options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(CustomOptions.class);
    final var pipeline = Pipeline.create(options);
    run(pipeline, options);
  }
  /**
   * Executes the pipeline with the provided execution parameters.
   *
   * @param pipeline The dataflow pipeline.
   */
  public static void run(final Pipeline pipeline, final CustomOptions options) {
    /*
     * Steps:
     *  1) Read from Big Query source
     *  2) Transform TableRows to a json string
     *  3) Write each text record to Pub/Sub
     */
    pipeline
        .apply(
            "Read from BigQuery query",
            BigQueryIO.readTableRows()
                .from(options.getBigQueryTableName())
                .withTemplateCompatibility()
                .withoutValidation())
        .apply(
            "TableRows -> PubSub Messages",
            MapElements.into(TypeDescriptor.of(String.class))
                .via(
                    (final TableRow item) -> {
                      // Data should be immutable from one PTransform to the next
                      final var tableRow = item == null ? new TableRow() : item.clone();
                      tableRow.setFactory(Utils.getDefaultJsonFactory());
                      return tableRow.toString();
                    }))
        .apply("Write to PubSub", PubsubIO.writeStrings().to(options.getOutputTopic()));

    pipeline.run();
  }
}
