package com.doit.dataflowtemplates;

import com.google.api.client.googleapis.util.Utils;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult.State;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.TypeDescriptor;

/**
 * The {@code BigQueryToPubSub} pipeline publishes records to Cloud Pub/Sub from a Big Query Table.
 *
 * <p>Example Usage:
 *
 * <pre>
 * {@code mvn compile exec:java \
 * -Dexec.mainClass=com.doit.dataflowtemplates.BigQueryToPubSub \
 * -Dexec.args=" \
 * --project=${PROJECT_ID} \
 * --stagingLocation=gs://${PROJECT_ID}/dataflow/pipelines/${PIPELINE_FOLDER}/staging \
 * --tempLocation=gs://${PROJECT_ID}/dataflow/pipelines/${PIPELINE_FOLDER}/temp \
 * --runner=DataflowRunner \
 * --bigQueryTableName= project.dataset.table \
 * --outputTopic=projects/${PROJECT_ID}/topics/${TOPIC_NAME}"
 * }
 * </pre>
 */
public class BigQueryToPubSub {

  /** The custom options supported by the pipeline. Inherits standard configuration options. */
  public interface Options extends PipelineOptions {

    @Description(
        "The name of the topic which data should be published to. "
            + "The name should be in the format of projects/<project-id>/topics/<topic-name>.")
    @Required
    ValueProvider<String> getOutputTopic();

    void setOutputTopic(ValueProvider<String> value);

    @Description(
        "The name of the Big Query table in full "
            + "The name should be in the format of project:dataset.table")
    @Required
    ValueProvider<String> getBigQueryTableName();

    void setBigQueryTableName(ValueProvider<String> value);
  }

  /**
   * Main entry-point for the pipeline. Reads in the command-line arguments, parses them, and
   * executes the pipeline.
   *
   * @param args Arguments passed in from the command-line.
   */
  public static void main(String[] args) {
    // Parse the user options passed from the command-line
    final Options options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);

    run(options);
  }

  /**
   * Executes the pipeline with the provided execution parameters.
   *
   * @param options The execution parameters.
   */
  public static State run(final Options options) {
    // Create the pipeline.
    final Pipeline pipeline = Pipeline.create(options);

    /*
     * Steps:
     *  1) Read from Big Query source
     *  2) Transform TableRows to a json string
     *  3) Write each text record to Pub/Sub
     */
    pipeline
        .apply(
            "Read from BigQuery query",
            BigQueryIO.readTableRows().from(options.getBigQueryTableName()))
        .apply(
            "TableRows -> PubSub Messages",
            MapElements.into(TypeDescriptor.of(String.class))
                .via(
                    (TableRow item) -> {
                      // Data should be immutable from one PTransform to the next
                      final TableRow tableRow = item.clone();
                      tableRow.setFactory(Utils.getDefaultJsonFactory());
                      return tableRow.toString();
                    }))
        .apply("Write to PubSub", PubsubIO.writeStrings().to(options.getOutputTopic()));

    return pipeline.run().waitUntilFinish();
  }
}
