package io.github.cernier.yml2props;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;
import org.yaml.snakeyaml.Yaml;

/**
 * Converts YAML resources files to Java's format <code>.properties</code> files.
 */
@Mojo(name = "convert", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class Yml2PropsConvertMojo extends AbstractMojo {

  /**
   * Comma-separated concatenation of list of patterns that follow the {@link Scanner#setIncludes(String[])}'s "Maven
   * standard", specifying the resources to convert.
   * 
   * @since 1.0.0
   */
  @Parameter(property = "includes", defaultValue = "**/*.yml,**/*.yaml", required = true)
  private String includes;

  /**
   * The root directory where to find resources to convert, according to <code>includes</code> parameter.
   * 
   * @since 1.0.0
   */
  @Parameter(property = "inputDirectory", defaultValue = "${project.build.outputDirectory}", required = true)
  private File inputDirectory;

  /**
   * Name of one of {@link StandardCharsets} to use for reading input files to convert.
   * 
   * @since 1.0.0
   */
  @Parameter(property = "inputCharset", defaultValue = "UTF-8", required = true)
  private String inputCharset;

  /**
   * Name of one of {@link StandardCharsets} to use for writing output files after conversion.
   * 
   * @since 1.0.0
   */
  @Parameter(property = "outputCharset", defaultValue = "UTF-8", required = true)
  private String outputCharset;

  /**
   * Whether original file should be deleted after successful conversion.
   * 
   * @since 1.0.0
   */
  @Parameter(property = "deleteOriginalFileAfterSuccessfulConversion", defaultValue = "true", required = true)
  private boolean deleteOriginalFileAfterSuccessfulConversion;

  public void setIncludes(String includes) {
    this.includes = includes;
  }

  public void setInputDirectory(File inputDirectory) {
    this.inputDirectory = inputDirectory;
  }

  public void setInputCharset(String inputCharset) {
    this.inputCharset = inputCharset;
  }

  public void setOutputCharset(String outputCharset) {
    this.outputCharset = outputCharset;
  }

  public void setDeleteOriginalFileAfterSuccessfulConversion(boolean deleteOriginalFileAfterSuccessfulConversion) {
    this.deleteOriginalFileAfterSuccessfulConversion = deleteOriginalFileAfterSuccessfulConversion;
  }

  public void execute() throws MojoExecutionException {
    for (String fileName : relativeFilesToConvert()) {
      try {
        File sourceFile = new File(inputDirectory, fileName);
        convertFile(sourceFile);
        if (deleteOriginalFileAfterSuccessfulConversion) {
          sourceFile.delete();
        }
      } catch (IOException e) {
        new MojoExecutionException(String.format("Failed to convert: %s", fileName), e);
      }
    }
  }

  private String[] relativeFilesToConvert() {
    final DirectoryScanner dirScanner = new DirectoryScanner();
    dirScanner.setBasedir(inputDirectory);
    dirScanner.setIncludes(StringUtils.split(includes, ","));
    dirScanner.scan();
    return dirScanner.getIncludedFiles();
  }

  private void convertFile(File ymlFile) throws IOException {
    InputStreamReader isr = new InputStreamReader(new FileInputStream(ymlFile), inputCharset);
    @SuppressWarnings("unchecked")
    Properties properties = toProperties(new Yaml().loadAs(isr, TreeMap.class));
    File propertyFile = new File(ymlFile.getParent(), FilenameUtils.removeExtension(ymlFile.getName()) + ".properties");
    properties.store(new OutputStreamWriter(new FileOutputStream(propertyFile), Charset.forName(outputCharset)), "");
  }

  private static Properties toProperties(Map<String, Object> propertiesMap) {
    return toProperties(propertiesMap, "");
  }

  private static Properties toProperties(Map<String, Object> propertiesMap, String keyPrefix) {
    return toProperties(propertiesMap, keyPrefix, new Properties());
  }

  @SuppressWarnings("unchecked")
  private static Properties toProperties(Object propertiesNode, String keyPrefix, Properties properties) {
    if (propertiesNode instanceof Map) {
      return toProperties((Map<String, Object>) propertiesNode, keyPrefix, properties);
    } else if (propertiesNode instanceof List) {
      return toProperties(((List<Object>) propertiesNode), keyPrefix, properties);
    } else {
      properties.put(keyPrefix, propertiesNode.toString());
    }
    return properties;
  }

  private static Properties toProperties(Map<String, Object> propertiesMap, String keyPrefix, Properties properties) {
    propertiesMap.forEach((key, value) -> {
      String fullKey = StringUtils.isNotEmpty(keyPrefix) ? String.format("%s.%s", keyPrefix, key) : key;
      toProperties(value, fullKey, properties);
    });
    return properties;
  }

  private static Properties toProperties(List<Object> propertiesList, String keyPrefix, Properties properties) {
    propertiesList.forEach(item -> {
      String fullKey = String.format("%s[%d]", keyPrefix, propertiesList.indexOf(item));
      toProperties(item, fullKey, properties);
    });
    return properties;
  }

}
