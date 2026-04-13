package com.github.fge.jsonschema.core.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Compatibility helpers while migrating from jackson 2 (com.fasterxml.*)
 * to jackson 3 (tools.jackson.*).
 */
public final class Jackson3Compat
{
    private Jackson3Compat()
    {
    }

    public static JsonNodeFactory nodeFactory()
    {
        return JsonNodeFactory.instance;
    }

    public static Map<String, JsonNode> asMap(final JsonNode node)
    {
        final Map<String, JsonNode> map = Maps.newLinkedHashMap();
        for (final String name : node.propertyNames())
            map.put(name, node.get(name));
        return map;
    }

    public static NodeType getNodeType(final JsonNode node)
    {
        if (node.isArray())
            return NodeType.ARRAY;
        if (node.isObject())
            return NodeType.OBJECT;
        if (node.isString())
            return NodeType.STRING;
        if (node.isBoolean())
            return NodeType.BOOLEAN;
        if (node.isNull())
            return NodeType.NULL;
        if (node.isIntegralNumber())
            return NodeType.INTEGER;
        if (node.isNumber())
            return NodeType.NUMBER;
        return null;
    }

    public static JsonNode path(final JsonPointer pointer, final JsonNode baseNode)
    {
        return baseNode.at(pointer.toString());
    }
}
