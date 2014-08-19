function varargout = retrieve_feval(fun, varargin)

vars = varargin;
vals = cellfun(@(v) fetchin('base', v), vars, 'UniformOutput', false);

varargout = cell(1, nargout);
[varargout{:}] = feval(fun, vals{:});
