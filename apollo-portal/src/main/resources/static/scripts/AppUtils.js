appUtil.service('AppUtil', ['toastr', function (toastr) {

    return {
        errorMsg: function (response) {
            var msg = "Code:" + response.status;
            if (response.data.message != null) {
                msg += " Msg:" + response.data.message;
            }
            return msg;
        },
        parseParams: function (path) {
            if (!path) {
                return {};
            }
            if (path.indexOf('/') == 0) {
                path = path.substring(1, path.length);
            }
            var params = path.split("&");
            var result = {};
            params.forEach(function (param) {
                var kv = param.split("=");
                result[kv[0]] = kv[1];
            });
            return result;
        },
        collectData: function (response) {
            var data = [];
            response.entities.forEach(function (entity) {
                if (entity.code == 200){
                    data.push(entity.body);
                }else {
                    toastr.warning(entity.message);
                }
            });  
            return data;
        }
    }
}]);
