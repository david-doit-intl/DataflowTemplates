package com.doit.dataflowtemplates;

import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.options.ValueProvider;

public interface CustomOptions extends PipelineOptions {

  @Description(
      "The name of the topic which data should be published to. "
          + "The name should be in the format of projects/<project-id>/topics/<topic-name>.")
  @Validation.Required
  ValueProvider<String> getOutputTopic();

  void setOutputTopic(ValueProvider<String> value);

  @Description(
      "The name of the Big Query table in full "
          + "The name should be in the format of project:dataset.table")
  @Validation.Required
  ValueProvider<String> getBigQueryTableName();

  void setBigQueryTableName(ValueProvider<String> value);
}
