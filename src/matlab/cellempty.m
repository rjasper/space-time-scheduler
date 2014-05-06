function varargout = cellempty

parfor i = 1:nargout
    varargout{i} = {};
end