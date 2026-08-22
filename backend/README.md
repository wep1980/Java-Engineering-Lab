# Backend — Java Engineering Lab

Java 21 + Spring Boot + Maven.

Documentação do projeto como um todo, incluindo como executar o ambiente
completo, está no [README raiz](../README.md) e em
[`specs/architecture/`](../specs/architecture/).

## Desenvolvimento local

```bash
mvn spring-boot:run
```

- Health-check: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI (JSON): http://localhost:8080/v3/api-docs

## Testes

```bash
mvn test
```
