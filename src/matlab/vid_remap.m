function [V_, vid_, idx_map, varargout] = vid_remap(V, vid, offset, varargin)

% determine relevant vids
if iscell(vid)
    vid_merge = [vid{:}];
else
    vid_merge = vid;
end

if offset > 0
    vid_all = setdiff(vid_merge, 1:offset);
else
    vid_all = unique(vid_merge);
end

% calculate old to new index mapping
idx_map = inverse_order([1:offset vid_all]);

% translate indices
V_ = V(:, vid_all - offset);
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