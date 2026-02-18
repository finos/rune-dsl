package com.regnosys.rosetta.tools.dto;

import com.google.common.collect.Multimap;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.REnumType;
import com.regnosys.rosetta.types.RType;
import com.rosetta.util.DottedPath;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportDtoGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportDtoGenerator.class);
    private static final String INDENT = "    ";

    private final JavaTypeTranslator javaTypeTranslator;
    private final ReportDtoTypeMapGenerator reportDtoTypeMapGenerator;

    @Inject
    public ReportDtoGenerator(JavaTypeTranslator javaTypeTranslator, ReportDtoTypeMapGenerator reportDtoTypeMapGenerator) {
        this.javaTypeTranslator = javaTypeTranslator;
        this.reportDtoTypeMapGenerator = reportDtoTypeMapGenerator;
    }


    public void generate(List<RosettaModel> models, DottedPath namespace, Path outputDirectory) {
        Multimap<RType, RAttribute> typeAttributeMap = reportDtoTypeMapGenerator.generateReportDtoTypeMap(models, namespace);
        typeAttributeMap.keySet().forEach(rType -> {
            LOGGER.info("Generating DTO class for {}.{}", rType.getNamespace().withDots(), rType.getName());
            Set<RAttribute> rAttributes = new HashSet<>(typeAttributeMap.get(rType));
            String content = generateClassBody(rType, rAttributes);
            Path path = outputDirectory.resolve(Path.of(rType.getNamespace().withForwardSlashes())).resolve(getClassName(rType) + ".java");
            writeFile(path, content);
        });
    }

    private String generateClassBody(RType rType, Set<RAttribute> rAttributes) {
        LOGGER.info("Generating class body for {}.{} with {} attributes", rType.getNamespace().withDots(), rType.getName(), rAttributes.size());
        String content = getPackageDeclaration(rType) +
                "\n" +
                getImports() +
                "\n" +
                getClassDeclaration(rType) +
                "\n" +
                getFieldDeclarations(rAttributes) +
                "\n" +
                getGetters(rAttributes) +
                "\n" +
                getEquals(rType, rAttributes) +
                "\n" +
                getHashCode(rAttributes) +
                "}\n";
        return content.replace("java.lang.", "");
    }

    private String getPackageDeclaration(RType rType) {
        return String.format("package %s;\n", rType.getNamespace().withDots());
    }

    private String getImports() {
        return """
                import com.fasterxml.jackson.annotation.JsonProperty;
                import java.util.List;
                import java.util.Objects;
                """;
    }

    private String getClassDeclaration(RType rType) {
        return String.format("public class %s {\n", getClassName(rType));
    }

    private String getFieldDeclarations(Set<RAttribute> rAttributes) {
        return rAttributes.stream().map(a ->
                        (a.getRMetaAnnotatedType().getRType() instanceof REnumType ? INDENT + "//" + javaTypeTranslator.toJavaType(a).toString() + "\n" : "") +
                                INDENT + String.format("@JsonProperty(\"%s\")\n", a.getName()) +
                                INDENT + String.format("private %s %s;\n", getAttributeType(a), a.getName()))
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    private String getGetters(Set<RAttribute> rAttributes) {
        return rAttributes.stream().map(this::getGetter)
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    private String getGetter(RAttribute rAttribute) {
        return INDENT + String.format("public %s get%s() {\n", getAttributeType(rAttribute), StringUtils.capitalize(rAttribute.getName())) +
                INDENT + INDENT + String.format("return %s;\n", rAttribute.getName()) +
                INDENT + "}\n";
    }

    private String getEquals(RType rType, Set<RAttribute> rAttributes) {
        return INDENT + "@Override\n" +
                INDENT + "public boolean equals(Object o) {\n" +
                INDENT + INDENT + "if (o == null || getClass() != o.getClass()) return false;\n" +
                INDENT + INDENT + String.format("%s that = (%s) o;\n", getClassName(rType), getClassName(rType)) +
                INDENT + INDENT + String.format("return %s;\n",
                rAttributes.stream()
                        .map(a -> String.format("Objects.equals(%s, that.%s)", a.getName(), a.getName()))
                        .sorted()
                        .collect(Collectors.joining(" &&\n" + INDENT + INDENT + INDENT))) +
                INDENT + "}\n";
    }

    private String getHashCode(Set<RAttribute> rAttributes) {
        return INDENT + "@Override\n" +
                INDENT + "public int hashCode() {\n" +
                INDENT + INDENT + String.format("return Objects.hash(%s);\n",
                rAttributes.stream()
                        .map(RAttribute::getName)
                        .sorted()
                        .collect(Collectors.joining(",\n" + INDENT + INDENT + INDENT))) +
                INDENT + "}\n";
    }

    private String getClassName(RType rType) {
        return rType.getName() + "Dto";
    }

    private String getAttributeType(RAttribute attribute) {
        String typeName = javaTypeTranslator.toJavaType(attribute).toString();
        if (attribute.getRMetaAnnotatedType().getRType() instanceof RDataType) {
            return typeName.contains(">") ? typeName.replace(">", "Dto>") : typeName + "Dto";
        } else if (attribute.getRMetaAnnotatedType().getRType() instanceof REnumType) {
            return attribute.isMulti() ? "List<String>" : "String";
        } else {
            return transformDateType(typeName);
        }
    }

    private String transformDateType(String typeName) {
        if (typeName.equals("com.rosetta.model.lib.records.Date")) {
            return "java.time.LocalDate";
        }
        return typeName;
    }

    private void writeFile(Path writePath, String content) {
        try {
            Files.createDirectories(writePath.getParent());
            Files.write(writePath, content.getBytes());
            LOGGER.info("Wrote output to {}", writePath);
        } catch (IOException e) {
            LOGGER.error("Failed to write output to {}", writePath, e);
        }
    }
}
