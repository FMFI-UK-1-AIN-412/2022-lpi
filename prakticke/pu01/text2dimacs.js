function VariableMap() {
  this.o = {};
  this.maxVar = 0;
}
VariableMap.prototype.get = function(v) {
  if (!(v in this.o)) {
    this.o[v] = ++this.maxVar;
  }
  return this.o[v];
}
VariableMap.prototype.list = function() {
  var o = this.o;
  return Object.keys(o)
    .map(function(k) {
      return [o[k], k];
    })
    .sort()
    .map(function(kv) {
      return 'c ' + kv[0].toString() + ' ' + kv[1];
    })
}

function variableNumberer(varMap) {
  return function(v) {
    if (v[0] === '¬' || v[0] === '-') {
      return -varMap.get(v.slice(1));
    } else {
      return varMap.get(v);
    }
  };
}

function lineConverter(varMap) {
  return function(line) {

    var items = line.trim().split(/\s/).filter((e) => !(e==='∨' || e==='v'));
    if (line.trim() === '') {
      return [''];
    }
    else if (items[0] == 'c') {
      return [line];
    }
    else {
      return ['c ' + line, items.map(variableNumberer(varMap)).join(' ') + ' 0' ];
    }
  };
}

function flatten(arrays) { return [].concat.apply([], arrays); }
function convertLines(lines, varMap) { return flatten(lines.map(lineConverter(varMap))); }

function doConvert()
{
  var lines = document.getElementById('in').value.split('\n');

  var varMap = new VariableMap();
  var converted = 
    convertLines(
      document.getElementById('in').value.split('\n'),
      varMap
    )
  var map = varMap.list();
  document.getElementById('out').value =
    [].concat(map, [''], converted).join('\n')
  ;
}

function onLoad()
{
  inArea = document.getElementById('in');
  outArea = document.getElementById('out');
  document.getElementById('convert').addEventListener('click', doConvert);

}

window.addEventListener('load', onLoad);
