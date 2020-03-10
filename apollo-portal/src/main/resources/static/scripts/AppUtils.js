appUtil.service('AppUtil', ['toastr', '$window', '$q', '$translate', function (toastr, $window, $q, $translate) {

    function parseErrorMsg(response) {
        if (response.status == -1) {
            return $translate.instant('Common.LoginExpiredTips');
        }
        var msg = "Code:" + response.status;
        if (response.data.message != null) {
            msg += " Msg:" + response.data.message;
        }
        return msg;
    }

    function parsePureErrorMsg(response) {
        if (response.status == -1) {
            return $translate.instant('Common.LoginExpiredTips');
        }
        if (response.data.message != null) {
            return response.data.message;
        }
        return "";
    }

    function ajax(resource, requestParams, requestBody) {
        var d = $q.defer();
        if (requestBody) {
            resource(requestParams, requestBody, function (result) {
                d.resolve(result);
            },
                function (result) {
                    d.reject(result);
                });
        } else {
            resource(requestParams, function (result) {
                d.resolve(result);
            },
                function (result) {
                    d.reject(result);
                });
        }

        return d.promise;
    }

    function diff(before, after) {
        /*
         Find the differences between two lists. Returns a list of pairs, where the
         first value is in ['+','-','='] and represents an insertion, deletion, or
         no change for that list. The second value of the pair is the list
         of elements.

         Params:
         before  the old list of immutable, comparable values (ie. a list
         of strings)
         after   the new list of immutable, comparable values

         Returns:
         A list of pairs, with the first part of the pair being one of three
         strings ('-', '+', '=') and the second part being a list of values from
         the original before and/or after lists. The first part of the pair
         corresponds to whether the list of values is a deletion, insertion, or
         unchanged, respectively.

         Examples:
         diff([1,2,3,4],[1,3,4])
         [["=",[1]],["-",[2]],["=",[4]]]

         diff([1,2,3,4],[2,3,4,1])
         [["-",[1]],["=",[2,3,4]],["+",[1]]]

         diff('The quick brown fox jumps over the lazy dog'.split(/[ ]+/),
         'The slow blue cheese drips over the lazy carrot'.split(/[ ]+/))
         [["=",["The"]],
         ["-",["quick","brown","fox","jumps"]],
         ["+",["slow","blue","cheese","drips"]],
         ["=",["over","the","lazy"]],
         ["-",["dog"]],
         ["+",["carrot"]]]
         */

        // Create a map from before values to their indices
        let oldIndexMap = {}, i;
        for (i = 0; i < before.length; i++) {
            oldIndexMap[before[i]] = oldIndexMap[before[i]] || [];
            oldIndexMap[before[i]].push(i);
        }

        // Find the largest substring common to before and after.
        // We use a dynamic programming approach here.
        // We iterate over each value in the `after` list.
        // At each iteration, `overlap[inew]` is the
        // length of the largest substring of `before.slice(0, iold)` equal
        // to a substring of `after.splice(0, iold)` (or unset when
        // `before[iold]` != `after[inew]`).
        // At each stage of iteration, the new `overlap` (called
        // `_overlap` until the original `overlap` is no longer needed)
        // is built from the old one.
        // If the length of overlap exceeds the largest substring
        // seen so far (`subLength`), we update the largest substring
        // to the overlapping strings.

        let overlap = [], startOld, startNew, subLength, inew;

        // `startOld` is the index of the beginning of the largest overlapping
        // substring in the before list. `startNew` is the index of the beginning
        // of the same substring in the after list. `subLength` is the length that
        // overlaps in both.
        // These track the largest overlapping substring seen so far, so naturally
        // we start with a 0-length substring.
        startOld = startNew = subLength = 0;

        for (inew = 0; inew < after.length; inew++) {
            let _overlap = [];
            oldIndexMap[after[inew]] = oldIndexMap[after[inew]] || [];
            for (i = 0; i < oldIndexMap[after[inew]].length; i++) {
                let iold = oldIndexMap[after[inew]][i];
                // now we are considering all values of val such that
                // `before[iold] == after[inew]`
                _overlap[iold] = ((iold && overlap[iold - 1]) || 0) + 1;
                if (_overlap[iold] > subLength) {
                    // this is the largest substring seen so far, so store its
                    // indices
                    subLength = _overlap[iold];
                    startOld = iold - subLength + 1;
                    startNew = inew - subLength + 1;
                }
            }
            overlap = _overlap;
        }

        if (subLength === 0) {
            // If no common substring is found, we return an insert and delete...
            var result = [];
            before.length && result.push(['-', before]);
            after.length && result.push(['+', after]);
            return result;
        }

        // ...otherwise, the common substring is unchanged and we recursively
        // diff the text before and after that substring
        return [].concat(
            diff(before.slice(0, startOld), after.slice(0, startNew)),
            [['=', after.slice(startNew, startNew + subLength)]],
            diff(before.slice(startOld + subLength), after.slice(startNew + subLength))
        );
    }

    function stringDiff(before, after) {
        /*
         Returns the difference between the old and new strings when split on
         whitespace. Considers punctuation a part of the word

         This function is intended as an example; you'll probably want
         a more sophisticated wrapper in practice.

         Params:
         before  the old string
         after   the new string

         Returns:
         the output of `diff` on the two strings after splitting them
         on whitespace (a list of change instructions; see the comment
         of `diff`)

         Examples:
         stringDiff('The quick brown fox', 'The fast blue fox')
         [["=",["The"]],
         ["-",["quick","brown"]],
         ["+",["fast","blue"]],
         ["=",["fox"]]]
         */
        return diff(before, after);
    }

    function htmlDiff(before, after) {
        /*
         Returns the difference between two strings (as in stringDiff) in
         HTML format. HTML code in the strings is NOT escaped, so you
         will get weird results if the strings contain HTML.

         This function is intended as an example; you'll probably want
         a more sophisticated wrapper in practice.

         Params:
         before  the old string
         after   the new string

         Returns:
         the output of the diff expressed with HTML <ins> and <del>
         tags.

         Examples:
         htmlDiff('The quick brown fox', 'The fast blue fox')
         'The <del>quick brown</del> <ins>fast blue</ins> fox'
         */
        let a, b, con, diff, i, results = [];
        con = {
            '=': function (x) {
                return x;
            },
            '+': function (x) {
                return '<ins>' + x + '</ins>';
            },
            '-': function (x) {
                return '<del>' + x + '</del>';
            }
        };

        diff = stringDiff(before, after);
        for (i = 0; i < diff.length; i++) {
            let chunk = diff[i];
            results.push(con[chunk[0]](chunk[1]));
        }

        return results.join('');
    }

    function isJson(target) {
        try{
            let result = jsl.parser.parse(target);
            if (result) {
                return true;
            }
        }catch (e) {
            return false;
        }
    }


    return {
        isJson: isJson,
        htmlDiff: htmlDiff,
        errorMsg: parseErrorMsg,
        pureErrorMsg: parsePureErrorMsg,
        ajax: ajax,
        showErrorMsg: function (response, title) {
            toastr.error(parseErrorMsg(response), title);
        },
        parseParams: function (query, notJumpToHomePage) {
            if (!query) {
                //如果不传这个参数或者false则返回到首页(参数出错)
                if (!notJumpToHomePage) {
                    $window.location.href = '/index.html';
                } else {
                    return {};
                }
            }
            if (query.indexOf('/') == 0) {
                query = query.substring(1, query.length);
            }

            var anchorIndex = query.indexOf('#');
            if (anchorIndex >= 0) {
                query = query.substring(0, anchorIndex);
            }

            var params = query.split("&");
            var result = {};
            params.forEach(function (param) {
                var kv = param.split("=");
                result[kv[0]] = decodeURIComponent(kv[1]);
            });
            return result;
        },
        collectData: function (response) {
            var data = [];
            response.entities.forEach(function (entity) {
                if (entity.code == 200) {
                    data.push(entity.body);
                } else {
                    toastr.warning(entity.message);
                }
            });
            return data;
        },
        showModal: function (modal) {
            $(modal).modal("show");
        },
        hideModal: function (modal) {
            $(modal).modal("hide");
        },
        checkIPV4: function (ip) {
            return /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$|^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\-]*[A-Za-z0-9])$|^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/.test(ip);
        }
    }
}]);
