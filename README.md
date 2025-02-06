
# ArchUnit-LikeC4-Rules

A Kotlin library that enforces architectural rules based on the **C4 Model** using **ArchUnit**. This library ensures that your code adheres to the architecture defined in your C4 diagrams, promoting modularity and clean dependencies.

## Features

- Validate that your code structure aligns with your C4 diagrams.
- Enforce strict module boundaries and dependencies.
- Load architecture models from local files or AWS S3.
- Easy integration with Kotlin and Java projects using Gradle.
- Support for custom architecture model loaders.

## Getting Started

### Installation

Add the following to your `build.gradle`:

Please notice, that you need to use your GitHub username and token to access the package.

```groovy
repositories {
    maven {
       url = uri("https://maven.pkg.github.com/syberry-corporation/archunit-likec4-rules")
       credentials {
          username = project.findProperty("gpr.user") ?: System.getenv("USERNAME")
          password = project.findProperty("gpr.key") ?: System.getenv("TOKEN")
       }
    }
    mavenCentral()
}

dependencies {
    testImplementation 'com.syberry.davinci:archunit-likec4-rules:0.0.2'
}
```

### Usage

#### Using JUnit with ArchUnit

You can easily integrate with JUnit for automated testing:

```kotlin
import com.syberry.davinci.archunit.rules.likec4.moduleStructureFromLikec4DiagramRule
import com.syberry.davinci.archunit.rules.likec4.loader.impl.S3ArchitectureArtifactLoader
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.ArchRule

@AnalyzeClasses(
    packages = ["com.yourapp"],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
class ArchitectureTests {

    @ArchTest
    val modulesRelations: ArchRule =
        moduleStructureFromLikec4DiagramRule(
            component = "omega.crm", // Path to the component from the diagram
            loader = S3ArchitectureArtifactLoader(bucketName = "your-bucket", key = "path/to/likec4.json")
        ).because(
            "Ensure module dependencies align with the architecture defined in the LikeC4 diagram."
        )
}
```

#### Implementing a Custom Loader

You can implement your own loader by creating a class that implements the `LikeC4ModelLoader` interface. This allows you to load architecture models from custom sources, such as databases, REST APIs, or local files.

##### Example:

```kotlin
import com.syberry.davinci.archunit.rules.likec4.loader.LikeC4ModelLoader

class CustomArchitectureLoader : LikeC4ModelLoader {
    override fun load(): String {
        // Load the LikeC4 model from a custom source
        return "{""model"": {""component"": ""CustomComponent""}}"
    }
}
```

You can then use the custom loader in your ArchUnit tests:

```kotlin
val rule = moduleStructureFromLikec4DiagramRule(
    component = "omega.crm", // Path to the component from the diagram
    loader = CustomArchitectureLoader()
)

rule.check(classes)
```

### Requirements for LikeC4 Diagram

For the library to work effectively, your LikeC4 diagram must meet the following requirements:

1. **Specification Section:** 
   - The diagram must include a `specification` section that defines elements such as `system`, `component`, and `module`.

2. **Metadata Package:** 
   - Each `module` must have a `metadata` block specifying the `package` associated with that module. This should align with the corresponding package structure in your codebase.

   Example:
   ```
   metadata {
     package '..crm.domain.account..'
   }
   ```

3. **Component Views:** 
   - Each `component` must have a dedicated view that includes all its modules. This ensures that the architecture validation covers the full scope of each component's internal structure.

   Example:
   ```
   view of omega.crm {
     include *
     include domain.*
   }
   ```

4. **Consistent Naming:** 
   - Module and package names in the LikeC4 diagram must correspond to the actual package names in your code.

5. **Defined Relationships:** 
   - Relationships between modules and components must be clearly specified using arrows (`->`). This helps enforce dependency rules during validation.

### How to Export LikeC4 Diagram

To export your LikeC4 diagram to JSON:

```bash
likec4 export json -o path/to/likec4.json
```

This JSON file will be used by the library to parse the architecture model and validate your code against it.

## Development Requirements

- **Kotlin 2.0.10**
- **Java 17**
- **Gradle**

## Contributing

Contributions are welcome! Feel free to open issues or submit pull requests to enhance the library.

## License

[MIT License](LICENSE)
