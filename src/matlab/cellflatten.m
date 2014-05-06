function varargout = cellflatten(varargin)

varargout = cellfun(@(C) [C{:}], varargin, 'UniformOutput', false);