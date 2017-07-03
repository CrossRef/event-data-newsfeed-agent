# Crossref Event Data Newsfeed Service

Agent to consume a collection of RSS and Atom newsfeeds.

## Tests

### Unit tests

 - `time docker-compose -f docker-compose-unit-tests.yml run -w /usr/src/app test lein test :unit`

## Config

Required:

Uses Event Data global configuration namespace.


 - `NEWSFEED_JWT`
 - `GLOBAL_ARTIFACT_URL_BASE`, e.g. https://artifact.eventdata.crossref.org
 - `GLOBAL_KAFKA_BOOTSTRAP_SERVERS`
 - `GLOBAL_STATUS_TOPIC`
