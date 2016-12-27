$(document).ready(function () {

    // nicescroll
    $("html").niceScroll({
                             styler: "fb",
                             cursorcolor: "#e8403f",
                             cursorwidth: '6',
                             cursorborderradius: '10px',
                             background: '#404040',
                             spacebarenabled: false,
                             cursorborder: '',
                             zindex: '1000'
                         });

    // bootstrap tooltip & textarea scroll
    setInterval(function () {
        $('[data-tooltip="tooltip"]').tooltip({
                                                  trigger : 'hover'
                                              });
        $("textarea").niceScroll({cursoropacitymax: 0});
        $("pre").niceScroll({cursoropacitymax: 0});
    }, 1000);

    setTimeout(function () {

        $(".release-history-list").niceScroll({cursoropacitymax: 0});

    }, 2500);
});

// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
// (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18 
Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份 
        "d+": this.getDate(), //日 
        "h+": this.getHours(), //小时 
        "m+": this.getMinutes(), //分 
        "s+": this.getSeconds(), //秒 
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度 
        "S": this.getMilliseconds() //毫秒 
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt =
                fmt.replace(RegExp.$1,
                            (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
};
