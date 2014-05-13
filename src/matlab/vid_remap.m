function [V_, vid_, idx_map, varargout] = vid_remap(V, vid, varargin)

% determine relevant vids
if iscell(vid)
    vid_all = unique([vid{:}]);
else
    vid_all = unique(vid);
end

% calculate old to new index mapping
idx_map = inverse_order(vid_all);

% translate indices
V_ = V(:, vid_all);
vid_ = remap(vid);
varargout = cellfun(@remap, varargin, 'UniformOutput', false);

    function vid_ = remap(vid)
        if iscell(vid)
            vid_ = cellfun(@helper, vid, 'UniformOutput', false);
        else
            vid_ = helper(vid);
        end
        
        function vid_ = helper(vid)
            vid_ = idx_map(vid);
        end
    end

end