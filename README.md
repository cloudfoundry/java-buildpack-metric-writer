# Cloud Foundry Spring Boot Metric Writer
This project provides a [Spring Boot `MetricWriter`][m] that writes metric updates to a Metron endpoint.

## Development
The project depends on Java 7.  To build from source and install to your local Maven cache, run the following:

```shell
$ ./mvnw clean install
```
## Contributing
[Pull requests][u] and [Issues][i] are welcome.

## License
This project is released under version 2.0 of the [Apache License][l].

[e]: http://docs.cloudfoundry.org/loggregator/architecture.html#metron
[i]: https://github.com/cloudfoundry/cf-java-client/issues
[l]: https://www.apache.org/licenses/LICENSE-2.0
[m]: http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-metric-writers
[u]: https://help.github.com/articles/using-pull-requests
