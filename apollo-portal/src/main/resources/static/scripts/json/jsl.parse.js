var jsl = typeof (jsl) === 'undefined' ? {} : jsl;
jsl.parser = function () {
    var a = !0, b = !1, c = {}, d = function () {
        var a = {
            trace: function () {
            },
            yy: {},
            symbols_: {
                error: 2,
                JSONString: 3,
                STRING: 4,
                JSONNumber: 5,
                NUMBER: 6,
                JSONNullLiteral: 7,
                NULL: 8,
                JSONBooleanLiteral: 9,
                TRUE: 10,
                FALSE: 11,
                JSONText: 12,
                JSONObject: 13,
                EOF: 14,
                JSONArray: 15,
                JSONValue: 16,
                "{": 17,
                "}": 18,
                JSONMemberList: 19,
                JSONMember: 20,
                ":": 21,
                ",": 22,
                "[": 23,
                "]": 24,
                JSONElementList: 25,
                $accept: 0,
                $end: 1
            },
            terminals_: {
                2: "error",
                4: "STRING",
                6: "NUMBER",
                8: "NULL",
                10: "TRUE",
                11: "FALSE",
                14: "EOF",
                17: "{",
                18: "}",
                21: ":",
                22: ",",
                23: "[",
                24: "]"
            },
            productions_: [0, [3, 1], [5, 1], [7, 1], [9, 1], [9, 1], [12, 2], [12, 2], [16, 1], [16, 1], [16, 1], [16, 1], [16, 1], [16, 1], [13, 2], [13, 3], [20, 3], [19, 1], [19, 3], [15, 2], [15, 3], [25, 1], [25, 3]],
            performAction: function (a, b, c, d, e, f, g) {
                var h = f.length - 1;
                switch (e) {
                    case 1:
                        this.$ = a;
                        break;
                    case 2:
                        this.$ = Number(a);
                        break;
                    case 3:
                        this.$ = null;
                        break;
                    case 4:
                        this.$ = !0;
                        break;
                    case 5:
                        this.$ = !1;
                        break;
                    case 6:
                        return this.$ = f[h - 1];
                    case 7:
                        return this.$ = f[h - 1];
                    case 8:
                        this.$ = f[h];
                        break;
                    case 9:
                        this.$ = f[h];
                        break;
                    case 10:
                        this.$ = f[h];
                        break;
                    case 11:
                        this.$ = f[h];
                        break;
                    case 12:
                        this.$ = f[h];
                        break;
                    case 13:
                        this.$ = f[h];
                        break;
                    case 14:
                        this.$ = {};
                        break;
                    case 15:
                        this.$ = f[h - 1];
                        break;
                    case 16:
                        this.$ = [f[h - 2], f[h]];
                        break;
                    case 17:
                        this.$ = {}, this.$[f[h][0]] = f[h][1];
                        break;
                    case 18:
                        this.$ = f[h - 2], f[h - 2][f[h][0]] = f[h][1];
                        break;
                    case 19:
                        this.$ = [];
                        break;
                    case 20:
                        this.$ = f[h - 1];
                        break;
                    case 21:
                        this.$ = [f[h]];
                        break;
                    case 22:
                        this.$ = f[h - 2], f[h - 2].push(f[h])
                }
            },
            table: [{12: 1, 13: 2, 15: 3, 17: [1, 4], 23: [1, 5]}, {1: [3]}, {14: [1, 6]}, {14: [1, 7]}, {
                3: 11,
                4: [1, 12],
                18: [1, 8],
                19: 9,
                20: 10
            }, {
                3: 18,
                4: [1, 12],
                5: 19,
                6: [1, 25],
                7: 16,
                8: [1, 22],
                9: 17,
                10: [1, 23],
                11: [1, 24],
                13: 20,
                15: 21,
                16: 15,
                17: [1, 4],
                23: [1, 5],
                24: [1, 13],
                25: 14
            }, {1: [2, 6]}, {1: [2, 7]}, {14: [2, 14], 18: [2, 14], 22: [2, 14], 24: [2, 14]}, {
                18: [1, 26],
                22: [1, 27]
            }, {18: [2, 17], 22: [2, 17]}, {21: [1, 28]}, {
                18: [2, 1],
                21: [2, 1],
                22: [2, 1],
                24: [2, 1]
            }, {14: [2, 19], 18: [2, 19], 22: [2, 19], 24: [2, 19]}, {22: [1, 30], 24: [1, 29]}, {
                22: [2, 21],
                24: [2, 21]
            }, {18: [2, 8], 22: [2, 8], 24: [2, 8]}, {18: [2, 9], 22: [2, 9], 24: [2, 9]}, {
                18: [2, 10],
                22: [2, 10],
                24: [2, 10]
            }, {18: [2, 11], 22: [2, 11], 24: [2, 11]}, {18: [2, 12], 22: [2, 12], 24: [2, 12]}, {
                18: [2, 13],
                22: [2, 13],
                24: [2, 13]
            }, {18: [2, 3], 22: [2, 3], 24: [2, 3]}, {18: [2, 4], 22: [2, 4], 24: [2, 4]}, {
                18: [2, 5],
                22: [2, 5],
                24: [2, 5]
            }, {18: [2, 2], 22: [2, 2], 24: [2, 2]}, {14: [2, 15], 18: [2, 15], 22: [2, 15], 24: [2, 15]}, {
                3: 11,
                4: [1, 12],
                20: 31
            }, {
                3: 18,
                4: [1, 12],
                5: 19,
                6: [1, 25],
                7: 16,
                8: [1, 22],
                9: 17,
                10: [1, 23],
                11: [1, 24],
                13: 20,
                15: 21,
                16: 32,
                17: [1, 4],
                23: [1, 5]
            }, {14: [2, 20], 18: [2, 20], 22: [2, 20], 24: [2, 20]}, {
                3: 18,
                4: [1, 12],
                5: 19,
                6: [1, 25],
                7: 16,
                8: [1, 22],
                9: 17,
                10: [1, 23],
                11: [1, 24],
                13: 20,
                15: 21,
                16: 33,
                17: [1, 4],
                23: [1, 5]
            }, {18: [2, 18], 22: [2, 18]}, {18: [2, 16], 22: [2, 16]}, {22: [2, 22], 24: [2, 22]}],
            defaultActions: {6: [2, 6], 7: [2, 7]},
            parseError: function (a, b) {
                throw new Error(a)
            },
            parse: function (a) {
                function o() {
                    var a;
                    a = b.lexer.lex() || 1, typeof a != "number" && (a = b.symbols_[a] || a);
                    return a
                }

                function n(a) {
                    c.length = c.length - 2 * a, d.length = d.length - a, e.length = e.length - a
                }

                var b = this, c = [0], d = [null], e = [], f = this.table, g = "", h = 0, i = 0, j = 0, k = 2, l = 1;
                this.lexer.setInput(a), this.lexer.yy = this.yy, this.yy.lexer = this.lexer, typeof this.lexer.yylloc == "undefined" && (this.lexer.yylloc = {});
                var m = this.lexer.yylloc;
                e.push(m), typeof this.yy.parseError == "function" && (this.parseError = this.yy.parseError);
                var p, q, r, s, t, u, v = {}, w, x, y, z;
                for (; ;) {
                    r = c[c.length - 1], this.defaultActions[r] ? s = this.defaultActions[r] : (p == null && (p = o()), s = f[r] && f[r][p]);
                    if (typeof s == "undefined" || !s.length || !s[0]) {
                        if (!j) {
                            z = [];
                            for (w in f[r]) this.terminals_[w] && w > 2 && z.push("'" + this.terminals_[w] + "'");
                            var A = "";
                            this.lexer.showPosition ? A = "Parse error on line " + (h + 1) + ":\n" + this.lexer.showPosition() + "\nExpecting " + z.join(", ") : A = "Parse error on line " + (h + 1) + ": Unexpected " + (p == 1 ? "end of input" : "'" + (this.terminals_[p] || p) + "'"), this.parseError(A, {
                                text: this.lexer.match,
                                token: this.terminals_[p] || p,
                                line: this.lexer.yylineno,
                                loc: m,
                                expected: z
                            })
                        }
                        if (j == 3) {
                            if (p == l) throw new Error(A || "Parsing halted.");
                            i = this.lexer.yyleng, g = this.lexer.yytext, h = this.lexer.yylineno, m = this.lexer.yylloc, p = o()
                        }
                        for (; ;) {
                            if (k.toString() in f[r]) break;
                            if (r == 0) throw new Error(A || "Parsing halted.");
                            n(1), r = c[c.length - 1]
                        }
                        q = p, p = k, r = c[c.length - 1], s = f[r] && f[r][k], j = 3
                    }
                    if (s[0] instanceof Array && s.length > 1) throw new Error("Parse Error: multiple actions possible at state: " + r + ", token: " + p);
                    switch (s[0]) {
                        case 1:
                            c.push(p), d.push(this.lexer.yytext), e.push(this.lexer.yylloc), c.push(s[1]), p = null, q ? (p = q, q = null) : (i = this.lexer.yyleng, g = this.lexer.yytext, h = this.lexer.yylineno, m = this.lexer.yylloc, j > 0 && j--);
                            break;
                        case 2:
                            x = this.productions_[s[1]][1], v.$ = d[d.length - x], v._$ = {
                                first_line: e[e.length - (x || 1)].first_line,
                                last_line: e[e.length - 1].last_line,
                                first_column: e[e.length - (x || 1)].first_column,
                                last_column: e[e.length - 1].last_column
                            }, u = this.performAction.call(v, g, i, h, this.yy, s[1], d, e);
                            if (typeof u != "undefined") return u;
                            x && (c = c.slice(0, -1 * x * 2), d = d.slice(0, -1 * x), e = e.slice(0, -1 * x)), c.push(this.productions_[s[1]][0]), d.push(v.$), e.push(v._$), y = f[c[c.length - 2]][c[c.length - 1]], c.push(y);
                            break;
                        case 3:
                            return !0
                    }
                }
                return !0
            }
        }, f = function () {
            var a = {
                EOF: 1, parseError: function (a, b) {
                    if (this.yy.parseError) this.yy.parseError(a, b); else throw new Error(a)
                }, setInput: function (a) {
                    this._input = a, this._more = this._less = this.done = !1, this.yylineno = this.yyleng = 0, this.yytext = this.matched = this.match = "", this.conditionStack = ["INITIAL"], this.yylloc = {
                        first_line: 1,
                        first_column: 0,
                        last_line: 1,
                        last_column: 0
                    };
                    return this
                }, input: function () {
                    var a = this._input[0];
                    this.yytext += a, this.yyleng++, this.match += a, this.matched += a;
                    var b = a.match(/\n/);
                    b && this.yylineno++, this._input = this._input.slice(1);
                    return a
                }, unput: function (a) {
                    this._input = a + this._input;
                    return this
                }, more: function () {
                    this._more = !0;
                    return this
                }, pastInput: function () {
                    var a = this.matched.substr(0, this.matched.length - this.match.length);
                    return (a.length > 20 ? "..." : "") + a.substr(-20).replace(/\n/g, "")
                }, upcomingInput: function () {
                    var a = this.match;
                    a.length < 20 && (a += this._input.substr(0, 20 - a.length));
                    return (a.substr(0, 20) + (a.length > 20 ? "..." : "")).replace(/\n/g, "")
                }, showPosition: function () {
                    var a = this.pastInput(), b = Array(a.length + 1).join("-");
                    return a + this.upcomingInput() + "\n" + b + "^"
                }, next: function () {
                    if (this.done) return this.EOF;
                    this._input || (this.done = !0);
                    var a, b, c, d;
                    this._more || (this.yytext = "", this.match = "");
                    var e = this._currentRules();
                    for (var f = 0; f < e.length; f++) {
                        b = this._input.match(this.rules[e[f]]);
                        if (b) {
                            d = b[0].match(/\n.*/g), d && (this.yylineno += d.length), this.yylloc = {
                                first_line: this.yylloc.last_line,
                                last_line: this.yylineno + 1,
                                first_column: this.yylloc.last_column,
                                last_column: d ? d[d.length - 1].length - 1 : this.yylloc.last_column + b[0].length
                            }, this.yytext += b[0], this.match += b[0], this.matches = b, this.yyleng = this.yytext.length, this._more = !1, this._input = this._input.slice(b[0].length), this.matched += b[0], a = this.performAction.call(this, this.yy, this, e[f], this.conditionStack[this.conditionStack.length - 1]);
                            if (a) return a;
                            return
                        }
                    }
                    if (this._input === "") return this.EOF;
                    this.parseError("Lexical error on line " + (this.yylineno + 1) + ". Unrecognized text.\n" + this.showPosition(), {
                        text: "",
                        token: null,
                        line: this.yylineno
                    })
                }, lex: function () {
                    var a = this.next();
                    return typeof a != "undefined" ? a : this.lex()
                }, begin: function (a) {
                    this.conditionStack.push(a)
                }, popState: function () {
                    return this.conditionStack.pop()
                }, _currentRules: function () {
                    return this.conditions[this.conditionStack[this.conditionStack.length - 1]].rules
                }
            };
            a.performAction = function (a, b, c, d) {
                var e = d;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        return 6;
                    case 2:
                        b.yytext = b.yytext.substr(1, b.yyleng - 2);
                        return 4;
                    case 3:
                        return 17;
                    case 4:
                        return 18;
                    case 5:
                        return 23;
                    case 6:
                        return 24;
                    case 7:
                        return 22;
                    case 8:
                        return 21;
                    case 9:
                        return 10;
                    case 10:
                        return 11;
                    case 11:
                        return 8;
                    case 12:
                        return 14;
                    case 13:
                        return "INVALID"
                }
            }, a.rules = [/^\s+/, /^-?([0-9]|[1-9][0-9]+)(\.[0-9]+)?([eE][-+]?[0-9]+)?\b/, /^"(\\["bfnrt/\\]|\\u[a-fA-F0-9]{4}|[^\0-\x09\x0a-\x1f"\\])*"/, /^\{/, /^\}/, /^\[/, /^\]/, /^,/, /^:/, /^true\b/, /^false\b/, /^null\b/, /^$/, /^./], a.conditions = {
                INITIAL: {
                    rules: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13],
                    inclusive: !0
                }
            };
            return a
        }();
        a.lexer = f;
        return a
    }();
    typeof a != "undefined" && typeof c != "undefined" && (c.parser = d, c.parse = function () {
        return d.parse.apply(d, arguments)
    }, c.main = function (b) {
        if (!b[1]) throw new Error("Usage: " + b[0] + " FILE");
        if (typeof process != "undefined") var d = a("fs").readFileSync(a("path").join(process.cwd(), b[1]), "utf8"); else var e = a("file").path(a("file").cwd()),
            d = e.join(b[1]).read({charset: "utf-8"});
        return c.parser.parse(d)
    }, typeof b != "undefined" && a.main === b && c.main(typeof process != "undefined" ? process.argv.slice(1) : a("system").args));
    return c
}()
