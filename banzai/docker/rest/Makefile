PROJECT_NAME := banzai_rest
BUILD_NUMBER := latest
DOCKER_REGISTRY=jodydadescott
DOCKER_IMAGE_NAME?=$(PROJECT_NAME)
DOCKER_IMAGE_TAG?=$(BUILD_NUMBER)

build:
	rm -rf cache
	mkdir -p cache
	cp -r app cache
	cp ../../rest/target/clouddb-rest.jar cache/app/lib/app.jar
	docker build -t $(DOCKER_REGISTRY)/$(DOCKER_IMAGE_NAME):$(DOCKER_IMAGE_TAG) .

push:
	docker push $(DOCKER_REGISTRY)/$(DOCKER_IMAGE_NAME):$(DOCKER_IMAGE_TAG)
