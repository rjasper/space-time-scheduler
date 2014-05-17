function varargout = flipline(varargin)

varargout = cellfun(@(line) line([3:4 1:2], :), varargin, 'UniformOutput', false);

end