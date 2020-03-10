var jsl = typeof jsl === 'undefined' ? {} : jsl;

jsl.format = (function () {

    function repeat(s, count) {
        return new Array(count + 1).join(s);
    }

    function formatJson(json) {
        var i = 0,
            il = 0,
            tab = "    ",
            newJson = "",
            indentLevel = 0,
            inString = false,
            currentChar = null;

        for (i = 0, il = json.length; i < il; i += 1) {
            currentChar = json.charAt(i);

            switch (currentChar) {
                case '{':
                case '[':
                    if (!inString) {
                        newJson += currentChar + "\n" + repeat(tab, indentLevel + 1);
                        indentLevel += 1;
                    } else {
                        newJson += currentChar;
                    }
                    break;
                case '}':
                case ']':
                    if (!inString) {
                        indentLevel -= 1;
                        newJson += "\n" + repeat(tab, indentLevel) + currentChar;
                    } else {
                        newJson += currentChar;
                    }
                    break;
                case ',':
                    if (!inString) {
                        newJson += ",\n" + repeat(tab, indentLevel);
                    } else {
                        newJson += currentChar;
                    }
                    break;
                case ':':
                    if (!inString) {
                        newJson += ": ";
                    } else {
                        newJson += currentChar;
                    }
                    break;
                case ' ':
                case "\n":
                case "\t":
                    if (inString) {
                        newJson += currentChar;
                    }
                    break;
                case '"':
                    if (i > 0 && json.charAt(i - 1) !== '\\') {
                        inString = !inString;
                    }
                    newJson += currentChar;
                    break;
                default:
                    newJson += currentChar;
                    break;
            }
        }

        return newJson;
    }

    return { "formatJson": formatJson };

} ());
