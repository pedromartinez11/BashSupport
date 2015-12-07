package com.ansorgit.plugins.bash.lang.parser.eval;

import com.ansorgit.plugins.bash.lang.BashVersion;
import com.ansorgit.plugins.bash.lang.lexer.BashLexer;
import com.ansorgit.plugins.bash.lang.lexer.BashTokenTypes;
import com.intellij.lexer.EmptyLexer;
import com.intellij.psi.tree.IElementType;
import org.junit.Assert;
import org.junit.Test;

public class PrefixSuffixAddingLexerTest {
    @Test
    public void testEmptyDelegate() throws Exception {
        String prefix = "prefix ";
        String suffix = " suffix";
        PrefixSuffixAddingLexer lexer = new PrefixSuffixAddingLexer(new EmptyLexer(), prefix, BashTokenTypes.STRING2, suffix, BashTokenTypes.STRING2);

        assertPosition(lexer, 0, prefix.length(), 0, BashTokenTypes.STRING2, prefix);

        lexer.advance();
        assertPosition(lexer, prefix.length(), prefix.length() + suffix.length(), 0, BashTokenTypes.STRING2, suffix);
        //after the first advance the delegate must still be at initial position
        Assert.assertEquals(0, lexer.getDelegate().getTokenStart());

        lexer.advance();
        Assert.assertNull(lexer.getTokenType());
        assertPosition(lexer, prefix.length() + suffix.length(), prefix.length() + suffix.length(), 0, null, "");
    }

    @Test
    public void testBasicDelegate() throws Exception {
        String prefix = "prefix ";
        String suffix = " suffix";

        PrefixSuffixAddingLexer lexer = new PrefixSuffixAddingLexer(new BashLexer(BashVersion.Bash_v4), prefix, BashTokenTypes.STRING2, suffix, BashTokenTypes.STRING2);
        lexer.start("echo hello world");

        //before suffix
        assertPosition(lexer, 0, "prefix ".length(), 0, BashTokenTypes.STRING2, "prefix ");
        lexer.advance();

        //after "prefix echo"
        assertPosition(lexer, "prefix echo".length(), "prefix echo ".length(), 0, BashTokenTypes.WHITESPACE, " ");
        lexer.advance();

        //after "prefix echo "
        assertPosition(lexer, "prefix echo ".length(), "prefix echo hello".length(), 0, BashTokenTypes.WORD, "hello");
        lexer.advance();

        //after "prefix echo hello"
        assertPosition(lexer, "prefix echo hello".length(), "prefix echo hello ".length(), 0, BashTokenTypes.WHITESPACE, " ");
        lexer.advance();

        //after "prefix echo hello "
        assertPosition(lexer, "prefix echo hello ".length(), "prefix echo hello world".length(), 0, BashTokenTypes.WORD, "world");
        lexer.advance();

        //after "prefix echo hello world"
        assertPosition(lexer, "prefix echo hello world".length(), "prefix echo hello world suffix".length(), 0, BashTokenTypes.STRING2, " suffix");
        lexer.advance();

        //after the last position no more changes are accepted
        Assert.assertNull(lexer.getTokenType());
        assertPosition(lexer, "prefix echo hello world suffix".length(), "prefix echo hello world suffix".length(), 0, null, "");
    }

    private void assertPosition(PrefixSuffixAddingLexer lexer, int startOffset, int endOffset, int state, IElementType token, String tokenText) {
        Assert.assertEquals(startOffset, lexer.getTokenStart());
        Assert.assertEquals(endOffset, lexer.getTokenEnd());
        Assert.assertEquals(token, lexer.getTokenType());
        Assert.assertEquals(tokenText, lexer.getTokenText());
        Assert.assertEquals(state, lexer.getState());
    }
}