# StarBank

Plataforma bancaria digital desarrollada en Java bajo principios de Programación Orientada a Objetos (POO).

## Estructura del Proyecto

```text
starbank/
├── .gitignore
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── starbank/
    │   │       ├── dominio/      # Entidades y reglas de negocio
    │   │       ├── servicio/     # Lógica de operaciones y gestión
    │   │       └── Main.java     # Punto de entrada local
    │   └── resources/            # Archivos de configuración o datos
    └── test/
        └── java/
            └── starbank/
                ├── dominio/
                └── servicio/
```

## Ejecución Local

Compilar y ejecutar pruebas:
```bash
mvn test
```

Ejecutar aplicación:
```bash
mvn compile exec:java -Dexec.mainClass="starbank.Main"
```
