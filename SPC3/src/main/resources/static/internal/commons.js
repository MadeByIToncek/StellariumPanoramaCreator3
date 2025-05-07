function httpGetAsync(theUrl, callback) {
    const xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState === 4)
            callback(xmlHttp.responseText,xmlHttp.status);
    }
    xmlHttp.open("GET", theUrl, true); // true for asynchronous
    xmlHttp.send(null);
}

function httpDeleteAsync(url, body, callback) {
    const xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState === 4)
            callback(xmlHttp.responseText,xmlHttp.status);
    }
    xmlHttp.open("DELETE", url, true); // true for asynchronous
    xmlHttp.send(body);
}

function httpPutAsync(url, body, callback) {
    const xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState === 4)
            callback(xmlHttp.responseText,xmlHttp.status);
    }
    xmlHttp.open("PUT", url, true); // true for asynchronous
    xmlHttp.send(body);
}

function httpPatchAsync(url, body, callback) {
    const xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState === 4)
            callback(xmlHttp.responseText,xmlHttp.status);
    }
    xmlHttp.open("PATCH", url, true); // true for asynchronous
    xmlHttp.send(body);
}

function httpPostAsync(url, body, callback) {
    const xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState === 4)
            callback(xmlHttp.responseText,xmlHttp.status);
    }
    xmlHttp.open("POST", url, true); // true for asynchronous
    xmlHttp.send(body);
}

function format(str, ...values) {
    return str.replace(/{(\d+)}/g, function(match, index) {
        return typeof values[index] !== 'undefined' ? values[index] : match;
    });
}

const CountdownLatch = function (limit) {
    this.limit = limit;
    this.count = 0;
    this.waitBlock = function () {
    };
};

CountdownLatch.prototype.countDown = function (){
    this.count = this.count + 1;
    if(this.limit <= this.count){
        return this.waitBlock();
    }
};

CountdownLatch.prototype.await = function(callback){
    this.waitBlock = callback;
};

const sleep = ms => new Promise(r => setTimeout(r, ms));
