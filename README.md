# DataflowTemplates
Dataflow Templates


## Getting Started

### Requirements
- Java 8

### Building

```bash
./gradlew build
```

## Run

```bash
./gradlew clean shadowJar && \
 java -jar build/libs/dataflowtemplates-1.0-SNAPSHOT.jar
  --project=${PROJECT_ID} \
  --region=${REGION} \
  --stagingLocation=gs://${PROJECT_ID}/dataflow/pipelines/${PIPELINE_FOLDER}/staging \
  --tempLocation=gs://${PROJECT_ID}/dataflow/pipelines/${PIPELINE_FOLDER}/temp \
  --runner=DataflowRunner \
  --bigQueryTableName=project:dataset.table \
  --outputTopic=projects/${PROJECT_ID}/topics/${TOPIC_NAME}
```
