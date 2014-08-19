function assign_feval(vars, fun, varargin)

if ~iscell(vars)
    vars = {vars};
end

valid = cellfun(@isvarname, vars);

if ~all(valid)
    error('invalid variable ''%s''\n', vars{~valid});
else
    nout = numel(vars);
    res = cell(1, nout);
    
    [res{:}] = feval(fun, varargin{:});
    cellfun(@(v, r) assignin('base', v, r), vars, res);
end
