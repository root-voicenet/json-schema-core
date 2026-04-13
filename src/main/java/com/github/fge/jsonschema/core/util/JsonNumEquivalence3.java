package com.github.fge.jsonschema.core.util;

import tools.jackson.databind.JsonNode;
import com.google.common.base.Equivalence;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Numeric-aware equivalence for jackson 3 nodes.
 *
 * <p>Semantics: numbers are compared by numeric value (so 1 and 1.0 are
 * equivalent), container nodes are compared recursively.</p>
 */
public final class JsonNumEquivalence3
    extends Equivalence<JsonNode>
{
    private static final JsonNumEquivalence3 INSTANCE = new JsonNumEquivalence3();

    public static JsonNumEquivalence3 getInstance()
    {
        return INSTANCE;
    }

    private JsonNumEquivalence3()
    {
    }

    @Override
    protected boolean doEquivalent(final JsonNode a, final JsonNode b)
    {
        if (a == b)
            return true;
        if (a == null || b == null)
            return false;

        if (a.isNumber() && b.isNumber())
            return asCanonicalDecimal(a).compareTo(asCanonicalDecimal(b)) == 0;

        if (a.isArray() && b.isArray()) {
            if (a.size() != b.size())
                return false;
            for (int i = 0; i < a.size(); i++)
                if (!doEquivalent(a.get(i), b.get(i)))
                    return false;
            return true;
        }

        if (a.isObject() && b.isObject()) {
            if (!a.propertyNames().equals(b.propertyNames()))
                return false;
            for (final String name : a.propertyNames())
                if (!doEquivalent(a.get(name), b.get(name)))
                    return false;
            return true;
        }

        return a.equals(b);
    }

    @Override
    protected int doHash(final JsonNode node)
    {
        if (node == null)
            return 0;

        if (node.isNumber())
            return asCanonicalDecimal(node).hashCode();

        if (node.isArray()) {
            int h = 1;
            for (final JsonNode child : node)
                h = 31 * h + doHash(child);
            return h;
        }

        if (node.isObject()) {
            int h = 1;
            final List<String> names = new ArrayList<>(node.propertyNames());
            Collections.sort(names);
            for (final String name : names)
                h = 31 * h + 31 * name.hashCode() + doHash(node.get(name));
            return h;
        }

        return node.hashCode();
    }

    private static BigDecimal asCanonicalDecimal(final JsonNode node)
    {
        return node.decimalValue().stripTrailingZeros();
    }
}
