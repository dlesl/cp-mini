.PHONY: build
build:
	./gradlew shadowJar

.PHONY: test
test: test-local test-docker

test-local: build
	SCHEMA_REGISTRY=localhost:8081 \
	KAFKA=localhost:9092 \
	java -jar build/libs/cp-mini.jar bash ./test/test.sh
	@echo "Local tests passed"

test-docker: build
	docker compose build
	docker compose run test /mnt/test.sh
	docker compose down -t 0
	@echo "Docker tests passed"
