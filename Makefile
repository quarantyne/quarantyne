gen_bloom_filters:
	./gradlew -PmainClass=com.quarantyne.scripts.BloomFiltersBuilder run --warning-mode all

publish_docs:
	./gradlew :docs:clean :docs:asciidoc
  	aws s3 sync docs/build/asciidoc/html5 s3://docs.quarantyne.com --acl public-read