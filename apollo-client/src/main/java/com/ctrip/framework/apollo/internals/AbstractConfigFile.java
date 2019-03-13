package com.ctrip.framework.apollo.internals;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.enums.ConfigSourceType;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.google.common.collect.Lists;

/**
 * @author Jason Song(song_s@ctrip.com)
 */
public abstract class AbstractConfigFile implements ConfigFile, RepositoryChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(AbstractConfigFile.class);
    private static ExecutorService m_executorService;
    protected final ConfigRepository m_configRepository;
    protected final String m_namespace;
    protected final AtomicReference<Properties> m_configProperties;
    private final List<ConfigFileChangeListener> m_listeners = Lists.newCopyOnWriteArrayList();

    private volatile ConfigSourceType m_sourceType = ConfigSourceType.NONE;

    static {
        m_executorService = Executors.newCachedThreadPool(ApolloThreadFactory
                .create("ConfigFile", true));
    }

    public AbstractConfigFile(String namespace, ConfigRepository configRepository) {
        m_configRepository = configRepository;
        m_namespace = namespace;
        m_configProperties = new AtomicReference<>();
        initialize();
    }

    private void initialize() {
        try {
            m_configProperties.set(m_configRepository.getConfig());
            m_sourceType = m_configRepository.getSourceType();
        } catch (Throwable ex) {
            Tracer.logError(ex);
            logger.warn("Init Apollo Config File failed - namespace: {}, reason: {}.",
                    m_namespace, ExceptionUtil.getDetailMessage(ex));
        } finally {
            //register the change listener no matter config repository is working or not
            //so that whenever config repository is recovered, config could get changed
            m_configRepository.addChangeListener(this);
        }
    }

    @Override
    public String getNamespace() {
        return m_namespace;
    }

    protected abstract void update(Properties newProperties);

    @Override
    public synchronized void onRepositoryChange(String namespace, Properties newProperties) {
        if (newProperties.equals(m_configProperties.get())) {
            return;
        }
        Properties newConfigProperties = new Properties();
        newConfigProperties.putAll(newProperties);

        String oldValue = getContent();

        update(newProperties);
        m_sourceType = m_configRepository.getSourceType();

        String newValue = getContent();

        PropertyChangeType changeType = PropertyChangeType.MODIFIED;

        if (oldValue == null) {
            changeType = PropertyChangeType.ADDED;
        } else if (newValue == null) {
            changeType = PropertyChangeType.DELETED;
        }

        this.fireConfigChange(new ConfigFileChangeEvent(m_namespace, oldValue, newValue, changeType));

        Tracer.logEvent("Apollo.Client.ConfigChanges", m_namespace);
    }

    @Override
    public void addChangeListener(ConfigFileChangeListener listener) {
        if (!m_listeners.contains(listener)) {
            m_listeners.add(listener);
        }
    }

    @Override
    public boolean removeChangeListener(ConfigChangeListener listener) {
        return m_listeners.remove(listener);
    }

    @Override
    public ConfigSourceType getSourceType() {
        return m_sourceType;
    }

    private void fireConfigChange(final ConfigFileChangeEvent changeEvent) {
        for (final ConfigFileChangeListener listener : m_listeners) {
            m_executorService.submit(new Runnable() {
                @Override
                public void run() {
                    String listenerName = listener.getClass().getName();
                    Transaction transaction = Tracer.newTransaction("Apollo.ConfigFileChangeListener", listenerName);
                    try {
                        listener.onChange(changeEvent);
                        transaction.setStatus(Transaction.SUCCESS);
                    } catch (Throwable ex) {
                        transaction.setStatus(ex);
                        Tracer.logError(ex);
                        logger.error("Failed to invoke config file change listener {}", listenerName, ex);
                    } finally {
                        transaction.complete();
                    }
                }
            });
        }
    }
}
