# DataflowTemplates
Dataflow Templates


## Getting Started

### Requirements
- Java 11
- maven

## Stage

```bash
export GOOGLE_APPLICATION_CREDENTIALS=<service account json file>

export REGION=<your region her>

export PROJECT=<your project here>

export BUCKET=<your bucket here>

mvn compile exec:java \
  -Dexec.mainClass=com.doit.dataflowtemplates.BigQueryToPubSub \
  -Dexec.args="--runner=DataflowRunner \
              --project=$PROJECT \
              --region=$REGION
              --stagingLocation=gs://$BUCKET/staging \
              --tempLocation=gs://$BUCKET/temp \
              --templateLocation=gs://$BUCKET/templates/BigQueryToPubSub"

gsutil cp BigQueryToPubSub_metadata gs://$BUCKET/templates/BigQueryToPubSub_metadata
```
