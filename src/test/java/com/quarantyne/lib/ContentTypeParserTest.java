package com.quarantyne.lib;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import org.junit.Test;

public class ContentTypeParserTest {
  @Test
  public void testParser() {
    assertThat(ContentTypeParser.parse("some/content_type").getCharset()).isNull();
    assertThat(ContentTypeParser.parse("some/content_type;key").getCharset()).isNull();
    assertThat(ContentTypeParser.parse("some/content_type; key=value").getCharset()).isNull();
    assertThat(ContentTypeParser.parse("some/content_type;charset=utf-8").getCharset()).isEqualTo(Charsets.UTF_8);
    assertThat(ContentTypeParser.parse("some/content_type; charset=utf-8").getCharset()).isEqualTo(Charsets.UTF_8);
    assertThat(ContentTypeParser.parse("some/content_type; CHARSET=UTF-8").getCharset()).isEqualTo(Charsets.UTF_8);
    assertThat(ContentTypeParser.parse("some/content_type; charset = utf-8").getCharset()).isEqualTo(Charsets.UTF_8);
  }
}
