package io.swagger.codegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Scanner;

import io.swagger.codegen.template.ExactCopyTemplateEngine;
import io.swagger.codegen.template.MultiTemplateEngine;
import io.swagger.codegen.template.TemplateLocator;
import io.swagger.codegen.template.TemplatePath;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.swagger.codegen.template.MultiTemplateEngine.multiTemplateLocator;

public abstract class AbstractGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGenerator.class);

    private TemplateLocator templateLocator = multiTemplateLocator();


    /**
     * Writes a {@code String} to a file, creating any parent directories if necessary.
     *
     * @param filename the file path
     * @param contents the content
     * @return the new {@link File}
     * @throws IOException when unable to create directories
     */
    public File writeToFile(String filename, String contents) throws IOException {
        LOGGER.info("writing file " + filename);

        final File outputFile = new File(filename);

        if (outputFile.getParent() != null) {
            final File outputDirectory = new File(outputFile.getParent());

            if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
                throw new IOException("Creating directory failed: " + outputDirectory);
            }
        }

        try (Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {
            fileWriter.write(contents);
        }

        return outputFile;
    }

    /**
     * Read template file from classpath or filesystem.
     *
     * @param name template name
     * @return entire content of the template
     * @throws RuntimeException when any error occurs
     *
     * @deprecated Use {@link io.swagger.codegen.template.TemplateEngine} instead.
     */
    @Deprecated
    public String readTemplate(String name) {
        try {
            Reader reader = getTemplateReader(name);
            if (reader == null) {
                throw new RuntimeException("no file found");
            }
            Scanner s = new Scanner(reader).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        throw new RuntimeException("can't load template " + name);
    }

    /**
     * Returns a {@link Reader} to a template file on the classpath or filesystem.
     *
     * @param name template name
     * @return reader
     * @throws RuntimeException when any error occurs
     *
     * @deprecated Use {@link TemplateLocator} instead.
     */
    @Deprecated
    public Reader getTemplateReader(String name) {
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(getCPResourcePath(name));
            if (is == null) {
                is = new FileInputStream(new File(name)); // May throw but never return a null value
            }
            return new InputStreamReader(is, "UTF-8");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        throw new RuntimeException("can't load template " + name);
    }

    /**
     * Get the template file path with template dir prepended, and use the
     * library template if exists.
     *
     * @param config Codegen config
     * @param templateFile Template file
     * @return String Full template file path
     *
     * @deprecated Use {@link TemplateLocator} instead.
     */
    @Deprecated
    public String getFullTemplateFile(CodegenConfig config, String templateFile) {
        TemplatePath templatePath = templateLocator.findTemplateFile(config, templateFile);

        if (templatePath == null) {
            return null;
        } else {
            return templatePath.getPath();
        }
    }

    /**
     * Read file from classpath or filesystem.
     *
     * @param resourceFilePath file path to resource
     * @return entire content of the file, or {@code null} if an error occurred.
     *
     * @deprecated Use {@link ExactCopyTemplateEngine} instead.
     */
    @Deprecated
    public String readResourceContents(String resourceFilePath) {
        try (InputStream inputStream = getClass().getResourceAsStream(getCPResourcePath(resourceFilePath))) {
            return IOUtils.toString(inputStream, "utf-8");
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Checks if a template exists on the classpath
     *
     * @param name template name
     * @return {@code true} if the template exists, {@code false} otherwise
     *
     * @deprecated Use {@link TemplateLocator} instead.
     */
    @Deprecated
    public boolean embeddedTemplateExists(String name) {
        return this.getClass().getClassLoader().getResource(getCPResourcePath(name)) != null;
    }

    /**
     * Converts native file path to Java resource path.
     *
     * @param path template path
     * @return resource path
     *
     * @deprecated Use {@link TemplateLocator} instead.
     */
    @Deprecated
    public String getCPResourcePath(String path) {
        return templateLocator.getCPResourcePath(path);
    }
}
