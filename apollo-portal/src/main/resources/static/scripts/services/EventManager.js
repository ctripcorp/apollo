appService.service('EventManager', [function () {

    /**
     * subscribe EventType with any object
     * @type {string}
     */
    var ALL_OBJECT = '*';

    var eventRegistry = {};

    /**
     *
     * @param eventType acquired. event type
     * @param context   optional. event execute context
     * @param objectId  optional. subscribe object id and empty value means subscribe event type with all object
     */
    function emit(eventType, context, objectId) {
        if (!eventType) {
            return;
        }

        if (!eventRegistry[eventType]) {
            return;
        }

        if (!context) {
            context = {};
        }

        if (!objectId) {
            objectId = ALL_OBJECT;
        }

        var subscribers = eventRegistry[eventType][objectId];
        emitEventToSubscribers(subscribers, context);

        if (objectId == ALL_OBJECT) {
            return;
        }

        //emit event to subscriber which subscribed all object
        subscribers = eventRegistry[eventType][ALL_OBJECT];
        emitEventToSubscribers(subscribers);
    }

    function emitEventToSubscribers(subscribers, context) {
        if (subscribers) {
            subscribers.forEach(function (subscriber) {
                subscriber.callback(context);
            })
        }
    }

    /**
     *
     * @param eventType acquired. event type
     * @param callback  acquired. callback function when event emitted
     * @param objectId  optional. subscribe object id and empty value means subscribe event type with all object
     */
    function subscribe(eventType, callback, objectId) {
        if (!eventType || !callback) {
            return;
        }

        if (!objectId) {
            objectId = ALL_OBJECT;
        }

        var subscribedObjects = eventRegistry[eventType];
        if (!subscribedObjects) {
            subscribedObjects = {};
            eventRegistry[eventType] = subscribedObjects;
        }

        var callbacks = subscribedObjects[objectId];
        if (!callbacks) {
            callbacks = [];
            subscribedObjects[objectId] = callbacks;
        }

        var subscriber = {
            id: Math.random() * Math.random(),
            callback: callback
        };
        callbacks.push(subscriber);

        return subscriber.id;
    }

    /**
     * 
     * @param eventType  acquired. event type
     * @param subscriberId acquired. subscriber id which get from event manager when subscribe
     * @param objectId optional.    subscribe object id and empty value means subscribe event type with all object
     */
    function unsubscribe(eventType, subscriberId, objectId) {
        if (!eventType || !subscriberId) {
            return;
        }

        if (!objectId) {
            objectId = ALL_OBJECT;
        }

        var subscribers = eventRegistry[eventType] ?
                          eventRegistry[eventType][objectId] : undefined;

        if (!subscribers) {
            return;
        }

        subscribers.forEach(function (subscriber, index) {
            if (subscriber.id == subscriberId) {
                subscribers.splice(index, 1);
            }
        })

    }

    return {
        ALL_OBJECT: ALL_OBJECT,

        emit: emit,
        subscribe: subscribe,
        unsubscribe: unsubscribe,

        EventType: {
            REFRESH_NAMESPACE: 'refresh_namespace',
            PUBLISH_NAMESPACE: 'pre_public_namespace',
            MERGE_AND_PUBLISH_NAMESPACE: 'merge_and_publish_namespace',
            PRE_ROLLBACK_NAMESPACE: 'pre_rollback_namespace',
            ROLLBACK_NAMESPACE: 'rollback_namespace',
            EDIT_GRAY_RELEASE_RULES: 'edit_gray_release_rules',
            UPDATE_GRAY_RELEASE_RULES: 'update_gray_release_rules',
            PUBLISH_DENY: 'publish_deny',
            EMERGENCY_PUBLISH: 'emergency_publish',
            PRE_DELETE_NAMESPACE: 'pre_delete_namespace',
            DELETE_NAMESPACE: 'delete_namespace',
            DELETE_NAMESPACE_FAILED: 'delete_namespace_failed',
            CHANGE_ENV_CLUSTER: "change_env_cluster",
            SYNTAX_CHECK_TEXT_FAILED: "syntax_check_text_failed"
        }

    }
}]);
