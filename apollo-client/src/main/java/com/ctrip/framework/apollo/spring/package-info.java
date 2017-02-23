/**
 * This package contains Apollo Spring integration codes and enables the following features:<br/>
 * <p>1. Support Spring XML based configuration</p>
 * <ul>
 *   <li>&lt;apollo:config namespaces="someNamespace"/&gt; to inject configurations from Apollo into Spring Property
 *   Sources so that placeholders like ${someProperty} and @Value("someProperty") are supported.</li>
 * </ul>
 * <p>2. Support Spring Java based configuration</p>
 * <ul>
 *   <li>@EnableApolloConfig(namespaces={"someNamespace"}) to inject configurations from Apollo into Spring Property
 *   Sources so that placeholders like ${someProperty} and @Value("someProperty") are supported.</li>
 * </ul>
 *
 * With the above configuration, annotations like @ApolloConfig("someNamespace")
 * and @ApolloConfigChangeListener("someNamespace) are also supported.<br />
 * <br />
 * Requires Spring 3.1.1+
 */
package com.ctrip.framework.apollo.spring;