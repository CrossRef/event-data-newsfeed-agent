# Crossref Event Data Newsfeed Service

Agent to consume a collection of RSS and Atom newsfeeds.

## Tests

### Unit tests

 - `time docker-compose -f docker-compose-unit-tests.yml run -w /usr/src/app test lein test :unit`

## Config

Required:

 - `PERCOLATOR_URL_BASE` e.g. https://percolator.eventdata.crossref.org
 - `JWT_TOKEN`
 - `STATUS_SERVICE_BASE`
 - `ARTIFACT_BASE`, e.g. https://artifact.eventdata.crossref.org
 