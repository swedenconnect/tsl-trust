/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aaasec.tsltrust.weblogic;

import se.tillvaxtverket.tsltrust.weblogic.utils.HTMLInputFilter;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 *
 * @author stefan
 */

  
public class HTMLInputTest extends TestCase{

		protected HTMLInputFilter vFilter;
		
		protected void setUp() 
		{ 
			vFilter = new HTMLInputFilter( true );
		}
		
		protected void tearDown()
		{
			vFilter = null;
		}
		
		private void t( String input, String result )
		{
			Assert.assertEquals( result, vFilter.filter(input) );
		}
		
		public void test_basics()
		{
			t( "", "" );
			t( "hello", "hello" );
		}
		
		public void test_balancing_tags()
		{
			t( "<b>hello", "<b>hello</b>" );
			t( "<b>hello", "<b>hello</b>" );
			t( "hello<b>", "hello" );
			t( "hello</b>", "hello" );
			t( "hello<b/>", "hello" );
			t( "<b><b><b>hello", "<b><b><b>hello</b></b></b>" );
			t( "</b><b>", "" );
		}
		
		public void test_end_slashes()
		{
			t("<img>","<img />");
			t("<img/>","<img />");
			t("<b/></b>","");
		}
		
		public void test_balancing_angle_brackets()
		{
			if (vFilter.ALWAYS_MAKE_TAGS) {
				t("<img src=\"foo\"","<img src=\"foo\" />");
				t("i>","");
				t("<img src=\"foo\"/","<img src=\"foo\" />");
				t(">","");
				t("foo<b","foo");
				t("b>foo","<b>foo</b>");
				t("><b","");
				t("b><","");
				t("><b>","");
			} else {
				t("<img src=\"foo\"","&lt;img src=\"foo\"");
				t("b>","b&gt;");
				t("<img src=\"foo\"/","&lt;img src=\"foo\"/");
				t(">","&gt;");
				t("foo<b","foo&lt;b");
				t("b>foo","b&gt;foo");
				t("><b","&gt;&lt;b");
				t("b><","b&gt;&lt;");
				t("><b>","&gt;");
			}
		}
		
		public void test_attributes()
		{
			t("<img src=foo>","<img src=\"foo\" />"); 
			t("<img asrc=foo>","<img />");
			t("<img src=test test>","<img src=\"test\" />"); 
		}
		
		public void test_disallow_script_tags()
		{
			t("<script>","");
			if (vFilter.ALWAYS_MAKE_TAGS) { t("<script","");  } else { t("<script","&lt;script"); }
			t("<script/>","");
			t("</script>","");
			t("<script woo=yay>","");
			t("<script woo=\"yay\">","");
			t("<script woo=\"yay>","");
			t("<script woo=\"yay<b>","");
			t("<script<script>>","");
			t("<<script>script<script>>","script");
			t("<<script><script>>","");
			t("<<script>script>>","");
			t("<<script<script>>","");
		}
		
		public void test_protocols()
		{
			t("<a href=\"http://foo\">bar</a>", "<a href=\"http://foo\">bar</a>");
			// we don't allow ftp. t("<a href=\"ftp://foo\">bar</a>", "<a href=\"ftp://foo\">bar</a>");
			t("<a href=\"mailto:foo\">bar</a>", "<a href=\"mailto:foo\">bar</a>");
			t("<a href=\"javascript:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
			t("<a href=\"java script:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
			t("<a href=\"java\tscript:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
			t("<a href=\"java\nscript:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
			t("<a href=\"java" + vFilter.chr(1) + "script:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
			t("<a href=\"jscript:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
			t("<a href=\"vbscript:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
			t("<a href=\"view-source:foo\">bar</a>", "<a href=\"#foo\">bar</a>");
		}
		
		public void test_self_closing_tags()
		{
			t("<img src=\"a\">","<img src=\"a\" />");
			t("<img src=\"a\">foo</img>", "<img src=\"a\" />foo");
			t("</img>", "");
		}
		
		public void test_comments()
		{
			if (vFilter.STRIP_COMMENTS) {
				t("<!-- a<b --->", "");
			} else {
				t("<!-- a<b --->", "<!-- a&lt;b --->");
			}
		}

	}
	// ============================================ END-UNIT-TEST ===========================================