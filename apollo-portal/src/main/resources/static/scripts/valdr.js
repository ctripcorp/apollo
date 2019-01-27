app_module.config(appValdr);
setting_module.config(appValdr);

function appValdr(valdrProvider) {
    valdrProvider.addConstraints({
                                     'App': {
                                         'appId': {
                                             'size': {
                                                 'max': 32,
                                                 'message': 'AppId长度不能多于32个字符'
                                             },
                                             'required': {
                                                 'message': 'AppId不能为空'
                                             }
                                         },
                                         'appName': {
                                             'size': {
                                                 'max': 128,
                                                 'message': '应用名称长度不能多于128个字符'
                                             },
                                             'required': {
                                                 'message': '应用名称不能为空'
                                             }
                                         }
                                     }
                                 })
}

cluster_module.config(function (valdrProvider) {
    valdrProvider.addConstraints({
                                     'Cluster': {
                                         'clusterName': {
                                             'size': {
                                                 'max': 32,
                                                 'message': '集群名称长度不能多于32个字符'
                                             },
                                             'required': {
                                                 'message': '集群名称不能为空'
                                             }
                                         }
                                     }
                                 })
});

namespace_module.config(function (valdrProvider) {
    valdrProvider.addConstraints({
                                     'AppNamespace': {
                                         'namespaceName': {
                                             'size': {
                                                 'max': 32,
                                                 'message': 'Namespace名称长度不能多于32个字符'
                                             },
                                             'required': {
                                                 'message': 'Namespace名称不能为空'
                                             }
                                         },
                                         'comment': {
                                             'size': {
                                                 'max': 64,
                                                 'message': '备注长度不能多于64个字符'
                                             }
                                         }
                                     }
                                 })
});

application_module.config(function (valdrProvider) {
    valdrProvider.addConstraints({
                                     'Item': {
                                         'key': {
                                             'size': {
                                                 'max': 128,
                                                 'message': 'Key长度不能多于128个字符'
                                             },
                                             'required': {
                                                 'message': 'Key不能为空'
                                             }
                                         },
                                         'comment': {
                                             'size': {
                                                 'max': 64,
                                                 'message': '备注长度不能多于64个字符'
                                             }
                                         }
                                     },
                                     'Release': {
                                         'releaseName': {
                                             'size': {
                                                 'max': 64,
                                                 'message': 'Release Name长度不能多于64个字符'
                                             },
                                             'required': {
                                                 'message': 'Release Name不能为空'
                                             }
                                         },
                                         'comment': {
                                             'size': {
                                                 'max': 64,
                                                 'message': '备注长度不能多于64个字符'
                                             }
                                         }
                                     }
                                 })
});


