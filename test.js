sadsf
var express = require('express');
var app = express();
var _ = require('underscore');

var tests = [
    {
        title         : 'Apple'
    },
    {
        title         : 'Banana'
    }
];

var arr = _.map(tests, function(test) {
    return test.title;
});

console.log(arr.join(', ')
