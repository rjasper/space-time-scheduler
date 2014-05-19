function varargout = fliplr_all(varargin)

varargout = cellfun(@fliplr, varargin, 'UniformOutput', false);

end