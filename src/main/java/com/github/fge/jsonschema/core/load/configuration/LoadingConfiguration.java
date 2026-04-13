/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jsonschema.core.load.configuration;

import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.util.JacksonFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.github.fge.Frozen;
import com.github.fge.Thawed;
import com.github.fge.jsonschema.core.load.Dereferencing;
import com.github.fge.jsonschema.core.load.SchemaLoader;
import com.github.fge.jsonschema.core.load.URIManager;
import com.github.fge.jsonschema.core.load.download.URIDownloader;
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfiguration;
import com.github.fge.jsonschema.core.tree.CanonicalSchemaTree;
import com.github.fge.jsonschema.core.tree.InlineSchemaTree;
import com.google.common.collect.ImmutableMap;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.util.EnumSet;
import java.util.Map;

/**
 * Loading configuration (frozen instance)
 *
 * <p>With a loading configuration, you can influence the following aspects:</p>
 *
 * <ul>
 *     <li>what schemas should be preloaded;</li>
 *     <li>what URI schemes should be supported;</li>
 *     <li>whether we want to cache loaded schemas;</li>
 *     <li>how to resolve URIs (see {@link URITranslatorConfiguration});</li>
 *     <li>what dereferencing mode should be used.</li>
 * </ul>
 *
 * <p>The default configuration only preloads the core metaschemas for draft v4
 * and draft v3, and uses canonical dereferencing mode; it also uses the default
 * set of supported schemes:</p>
 *
 * <ul>
 *     <li>{@code file},</li>
 *     <li>{@code http},</li>
 *     <li>{@code https},</li>
 *     <li>{@code ftp},</li>
 *     <li>{@code resource} (resource in the classpath),</li>
 *     <li>{@code jar} (jar URL).</li>
 * </ul>
 *
 * <p>You don't instantiate this class directly, you must go through a {@link
 * LoadingConfigurationBuilder} for this (using {@link #newBuilder()};
 * alternatively, you can obtain a default configuration using {@link
 * #byDefault()}.</p>
 *
 * @see LoadingConfigurationBuilder
 * @see Dereferencing
 * @see URIManager
 * @see SchemaLoader
 */
public final class LoadingConfiguration
    implements Frozen<LoadingConfigurationBuilder>
{
    /**
     * Map of URI downloaders
     *
     * @see URIDownloader
     * @see URIManager
     */
    final Map<String, URIDownloader> downloaders;

    final URITranslatorConfiguration translatorCfg;
    
    /**
     * Cache size
     *
     * <p>Note that this do not affect preloaded schemas; these are always
     * cached.</p>
     */
    final int cacheSize;

    /**
     * Dereferencing mode
     *
     * @see SchemaLoader
     * @see CanonicalSchemaTree
     * @see InlineSchemaTree
     */
    final Dereferencing dereferencing;

    /**
     * Map of preloaded schemas
     */
    final Map<URI, JsonNode> preloadedSchemas;

    /**
     * Set of JsonParser features to be enabled while loading schemas
     *
     * <p>The set of JavaParser features used to construct ObjectMapper/
     * ObjectReader instances used to load schemas</p>
     */
    final EnumSet<StreamReadFeature> parserFeatures;
    final EnumSet<JsonReadFeature> jsonReadFeatures;

    /**
     * ObjectMapper configured with enabled parser features.
     */
    private final ObjectMapper reader;

    /**
     * Create a new, default, mutable configuration instance
     *
     * @return a {@link LoadingConfigurationBuilder}
     */
    public static LoadingConfigurationBuilder newBuilder()
    {
        return new LoadingConfigurationBuilder();
    }

    /**
     * Create a default, immutable loading configuration
     *
     * <p>This is the result of calling {@link Thawed#freeze()} on {@link
     * #newBuilder()}.</p>
     *
     * @return a default configuration
     */
    public static LoadingConfiguration byDefault()
    {
        return new LoadingConfigurationBuilder().freeze();
    }

    /**
     * Create a frozen loading configuration from a thawed one
     *
     * @param builder the thawed configuration
     * @see LoadingConfigurationBuilder#freeze()
     */
    LoadingConfiguration(final LoadingConfigurationBuilder builder)
    {
        downloaders = builder.downloaders.build();
        translatorCfg = builder.translatorCfg;
        dereferencing = builder.dereferencing;
        preloadedSchemas = ImmutableMap.copyOf(builder.preloadedSchemas);
        parserFeatures = EnumSet.copyOf(builder.parserFeatures);
        jsonReadFeatures = EnumSet.copyOf(builder.jsonReadFeatures);
        reader = buildReader();
        cacheSize = builder.cacheSize;
    }

    /**
     * Construct a configured mapper
     *
     * @return a JSON mapper
     */
    private ObjectMapper buildReader()
    {
        JsonMapper.Builder builder = JsonMapper.builder();

        // enable JsonParser feature configurations
        for (final StreamReadFeature feature : parserFeatures)
            builder = builder.configure(feature, true);

        for (final JsonReadFeature feature : jsonReadFeatures)
            builder = builder.enable(feature);

        return builder.build();
    }

    /**
     * Return the map of downloaders for this configuration
     *
     * @return an {@link ImmutableMap} of downloaders
     *
     * @since 1.1.9
     */
    public Map<String, URIDownloader> getDownloaderMap()
    {
        return downloaders; // ImmutableMap
    }

    public URITranslatorConfiguration getTranslatorConfiguration()
    {
        return translatorCfg;
    }

    /**
     * Return the dereferencing mode used for this configuration
     *
     * @return the dereferencing mode
     */
    public Dereferencing getDereferencing()
    {
        return dereferencing;
    }

    /**
     * Return the map of preloaded schemas
     *
     * @return an immutable map of preloaded schemas
     */
    public Map<URI, JsonNode> getPreloadedSchemas()
    {
        return preloadedSchemas;
    }

    /**
     * Get a configured JSON mapper
     *
     * @return the JSON mapper
     */
    public ObjectMapper getReader()
    {
        return reader;
    }
    
    /**
     * Return if we want to cache loaded schema or not
     * note that this do not affect preloadedSchema that are always cached
     * 
     * @deprecated Use cacheSize getter instead to get the cache size
     * 
     * @return if the cache has to be enabled
     */
    @Deprecated
    public boolean getEnableCache() {
        return this.cacheSize != 0;
    }
    
    /**
     * Return the size of the cache to use
     * note that this do not affect preloadedSchema that are always cached
     *
     * @return the size of the cache. A zero-value means that it is not enabled
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Return a thawed version of this loading configuration
     *
     * @return a thawed copy
     * @see LoadingConfigurationBuilder#LoadingConfigurationBuilder(LoadingConfiguration)
     */
    @Override
    public LoadingConfigurationBuilder thaw()
    {
        return new LoadingConfigurationBuilder(this);
    }
}
