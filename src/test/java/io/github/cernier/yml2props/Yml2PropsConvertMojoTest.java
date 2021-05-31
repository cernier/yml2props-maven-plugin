package io.github.cernier.yml2props;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.codehaus.plexus.PlexusTestCase.getBasedir;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class Yml2PropsConvertMojoTest {

  @Rule
  public MojoRule mojoRule = new MojoRule();

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    File testPom = new File(getBasedir(), "target/test-classes/plugin-config.xml");
    mojoUnderTest = (Yml2PropsConvertMojo) mojoRule.lookupMojo("convert", testPom);

    mojoUnderTest.setIncludes("**/*.yml,**/*.yaml");
    mojoUnderTest.setInputDirectory(testFolder.getRoot());
    mojoUnderTest.setInputCharset("UTF-8");
    mojoUnderTest.setOutputCharset("UTF-8");
    mojoUnderTest.setDeleteOriginalFileAfterSuccessfulConversion(true);
  }

  private Yml2PropsConvertMojo mojoUnderTest;

  @Test
  public void testConvertBasic() throws Exception {
    File directory = writeToFile("foo/bar.yml",
        "foo:",
        "  bar: 42"
    );

    mojoUnderTest.execute();

    Properties properties = loadPropertiesFromFile(directory, "bar.properties");

    assertEquals(1, properties.size());
    assertEquals("42", properties.getProperty("foo.bar"));
    assertFalse(new File(directory, "bar.yml").exists());
  }

  @Test
  public void testConvertComplexStructure() throws Exception {
    File directory = writeToFile("foo/bar.yml",
        "root:",
        "  foo:",
        "    bar: 42",
        "    items:",
        "      - prop1: 'value0.1'",
        "        prop2: 'value0.2'",
        "      - prop1: 'value1.1'",
        "        prop2: 'value1.2'"
    );

    mojoUnderTest.execute();

    Properties properties = loadPropertiesFromFile(directory, "bar.properties");

    assertEquals(5, properties.size());
    assertEquals("42", properties.getProperty("root.foo.bar"));
    assertEquals("value0.1", properties.getProperty("root.foo.items[0].prop1"));
    assertEquals("value0.2", properties.getProperty("root.foo.items[0].prop2"));
    assertEquals("value1.1", properties.getProperty("root.foo.items[1].prop1"));
    assertEquals("value1.2", properties.getProperty("root.foo.items[1].prop2"));
  }

  @Test
  public void testConvertOnlyIncluded() throws Exception {
    mojoUnderTest.setIncludes("included/**/*.yml");
    mojoUnderTest.setInputDirectory(testFolder.getRoot());
    File includedFileDirectory = writeToFile("included/foo/bar.yml",
        "included:",
        "  foo:",
        "    bar: 42"
    );
    File anotherIncludedFileDirectory = writeToFile("included/another/bar.yml",
        "included:",
        "  another:",
        "    bar: 42"
    );
    File excludedFileDirectory = writeToFile("excluded/foo/bar.yml",
        "excluded:",
        "  foo:",
        "    bar: 42"
    );
  
    mojoUnderTest.execute();

    assertEquals(1, loadPropertiesFromFile(includedFileDirectory, "bar.properties").size());
    assertEquals(1, loadPropertiesFromFile(anotherIncludedFileDirectory, "bar.properties").size());

    try {
      loadPropertiesFromFile(excludedFileDirectory, "bar.properties");
      fail("FileNotFoundException should have been thrown.");
    } catch (Exception e) {
      assertTrue(e instanceof FileNotFoundException);
    }
  }

  @Test
  public void testConvertWithCharsetSupport() throws Exception {
    File directory = writeToFileWithCharset("foo/bar.yml", "ISO-8859-1",
        "foo:",
        "  bar: caractères accentués"
    );

    // Execute with default (and wrong) charsets (UTF-8)
    mojoUnderTest.setDeleteOriginalFileAfterSuccessfulConversion(false); // to be able to re-execute a 2nd time
    mojoUnderTest.execute();

    assertNotEquals("caractères accentués", loadPropertiesFromFile(directory, "bar.properties").getProperty("foo.bar"));
    assertTrue(new File(directory, "bar.yml").exists());

    // Execute with appropriate charsets
    mojoUnderTest.setInputCharset("ISO-8859-1");
    mojoUnderTest.setOutputCharset("ISO-8859-1");
    mojoUnderTest.setDeleteOriginalFileAfterSuccessfulConversion(true); // restore default cleaning config parameter
    mojoUnderTest.execute();

    assertEquals("caractères accentués", loadPropertiesFromFile(directory, "bar.properties").getProperty("foo.bar"));
    assertFalse(new File(directory, "bar.yml").exists());
  }

  private File writeToFile(String fileName, String... lines) throws IOException {
    return writeToFileWithCharset(fileName, "UTF-8", lines);
  }

  private File writeToFileWithCharset(String fileName, String charsetName, String... lines) throws IOException {
    File directory = testFolder.newFolder(FilenameUtils.getPathNoEndSeparator(fileName).split("/"));
    File file = new File(directory, FilenameUtils.getName(fileName));
    PrintWriter printWriter = new PrintWriter(file, charsetName);
    for (String line : lines) {
      printWriter.println(line);
    }
    printWriter.close();
    return directory;
  }

  private Properties loadPropertiesFromFile(File directory, String fileName) throws Exception {
    Properties properties = new Properties();
    properties.load(new FileInputStream(new File(directory, fileName)));
    return properties;
  }

}
