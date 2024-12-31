#!/usr/bin/env bash

# Running the image as
#  - removing the container after exit (ease housekeeping because it's a POC)
#  - detached (-d),
#  - binding localhost:8080 to container:8080
# add a var env to set postgres on localhost (see specific run out of docker-compose)
docker run --rm -e POSTGRES_HOST=127.0.0.1 -d -p 8080:8080 pcollet/tcf-spring-backend

# to stop: docker stop ID
# to start a new shell in the container: docker exec -it ID bash
# to attach to the container: docker attach ID (^P ^Q to detach)
