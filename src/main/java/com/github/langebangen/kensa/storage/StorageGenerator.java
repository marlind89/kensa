package com.github.langebangen.kensa.storage;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;

public class StorageGenerator
{

    public static void main(String[] args)
        throws Exception
    {
        var config = new Configuration()
            // Configure the database connection here
            .withJdbc(new Jdbc()
                .withDriver("org.postgresql.Driver")
                .withUrl(args[0])
                .withUser(args[1])
                .withPassword(args[2])
            )
            .withGenerator(new Generator()
                .withDatabase(new Database()
                    .withName("org.jooq.meta.postgres.PostgresDatabase")

                    // All elements that are generated from your schema (A Java regular expression.
                    // Use the pipe to separate several expressions) Watch out for
                    // case-sensitivity. Depending on your database, this might be
                    // important!
                    //
                    // You can create case-insensitive regular expressions using this syntax: (?i:expr)
                    //
                    // Whitespace is ignored and comments are possible.
                    .withIncludes(".*")

                    // The schema that is used locally as a source for meta information.
                    // This could be your development schema or the production schema, etc
                    // This cannot be combined with the schemata element.
                    //
                    // If left empty, jOOQ will generate all available schemata. See the
                    // manual's next section to learn how to generate several schemata
                    .withInputSchema("public")
                )
                .withTarget(new Target()

                    // The destination package of your generated classes (within the
                    // destination directory)
                    //
                    // jOOQ may append the schema name to this package if generating multiple schemas,
                    // e.g. org.jooq.your.packagename.schema1
                    // org.jooq.your.packagename.schema2
                    .withPackageName("com.github.langebangen.kensa.storage.generated")
                    // The destination directory of your generated classes
                    .withDirectory("src/main/java")
                )
            );

        GenerationTool.generate(config);
    }
}
