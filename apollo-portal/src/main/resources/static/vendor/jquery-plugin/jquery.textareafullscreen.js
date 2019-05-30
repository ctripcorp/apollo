/*

 jQuery Textarea Fullscreen Editor v1.0
 Fullscreen text editor plugin for jQuery.

 :For more details visit http://github.com/CreoArt/jquery.textareafullscreen

 - CreoArt <support@creoart.org>
 - http://github.com/CreoArt

 Licensed under Apache - https://raw.githubusercontent.com/CreoArt/jquery.textareafullscreen/master/LICENSE

*/
(function($) {
    "use strict";

    var isFullscreen = false,
        $el,
        $wrapper,
        $editor,
        $icon,
        $overlay,
        transitionDuration = 300,
        sourceWidth,
        sourceHeight,
        settings = {
            overlay: true,
            maxWidth: '',
            maxHeight: ''
        };
    var methods = {

        init: function(opts) {

            settings = settings || {};
            $.extend(true, settings, settings);
            $.extend(true, settings, opts);

            $el = $(this);
            if (!$el.is('textarea')) {
                $.error(
                    'Error initializing Textarea Fullscreen Editor Plugin. It can only work on <textarea> element.'
                );
                return;
            }
            var content =
                '<div class="tx-editor-wrapper"><div class="tx-editor"><a href="#" class="tx-icon"></a></div></div>';
            var $wrapper = $(content).insertAfter(this);
            $editor = $wrapper.find('.tx-editor');
            $icon = $editor.find('.tx-icon');
            $editor.append($el);

            $el.css({
                'width': '100%',
                'height': '100%',
				'resize': 'none'
            });

            // ESC = closes the fullscreen mode
            $(window).on("keyup.txeditor", function(e) {
                if (e.keyCode == 27) {
                    isFullscreen ? methods.minimize() : '';
                }
            });

            // fullscreen icon click event
            $icon.on('click.txeditor.icon', function(e) {
                e.preventDefault();
                methods[isFullscreen ? "minimize" : "expand"]();
            });
            return this;
        },

        showOverlay: function() {
            $('<div class="tx-editor-overlay" />').appendTo('body')
                .fadeTo(0, 1)
                .click(function() {
                    methods.minimize();
                });
            return this;
        },

        removeOverlay: function() {
            var $overlay = $('.tx-editor-overlay');
            if ($overlay.length) {
                $overlay.fadeTo(0, 0, function() {
                    $(this).remove();
                });
            }
            return this;
        },

        expand: function() {
            settings.maxWidth ? $editor.css('max-width', settings.maxWidth) :
                '';
            settings.maxHeight ? $editor.css('max-height', settings.maxHeight) :
                '';
            if (settings.overlay) {
                methods.showOverlay();
            }

            $(window).on('resize.txeditor', function() {
                maxRelocate($editor);
            });

            $editor.addClass('expanded');
            transitions();

            return this;
        },

        minimize: function() {

            $(window).off('resize.txeditor', relocate($editor))
            $editor.removeClass('expanded')
                .css({
                    'max-width': 'none',
                    'max-height': 'none'
                });
            if (settings.overlay) {
                methods.removeOverlay();
            }
            transitions();

            return this;
        },

        destroy: function() {

            methods.removeOverlay();
            $wrapper.insertBefore($el);
            $wrapper.remove();

            $(window).off('keyup.txeditor')
                .off('resize.txeditor');

            return this;
        },
    };

    var transitions = function () {

            if (isFullscreen) {
				$el.focus();
				isFullscreen = false;
				relocate($editor);
            } else {
				$el.focus();
				$editor.css('opacity', 1);
				isFullscreen = true;
				maxRelocate($editor);
            }

            return;
    };

    var inited = false;
    function maxRelocate(el) {
        if (!inited) {
            sourceWidth = el.width();
            sourceHeight = el.height() / 2;
            inited = true;
        }
        var yPos = 0 | ((($(window).height() - el.height()) / 2));
        var xPos = 0 | (($(window).width() - el.width()) / 2);
        el.css({
            'top': -10,
            'left': 120 - xPos,
            'width': $(window).width() - 50,
            'height': $(window).height() - 40
        });
    }

    function relocate(el) {

            var yPos = 0 | ((($(window).height() - el.height()) / 2));
            var xPos = 0 | (($(window).width() - el.width()) / 2);
            el.css({
                'top': yPos,
                'left': xPos,
                'width': sourceWidth,
                'height': sourceHeight
            });
        }

    $.fn.textareafullscreen = function(method) {
        $.extend(methods, transitions);
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(
                arguments, 1));
        } else if (typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method +
                ' does not exist on jQuery.textareafullscreen');
        }
    };

})(jQuery);
