function eid_B = determine_bridge_eid(vid, vid_S, isB, side)

switch (side)
    case {'left' 'l'}
        vid_S_ = vid_S([1:end-1; 2:end]');
    case {'right' 'r'}
        vid_S_ = vid_S([2:end; 1:end-1]');
end

if iscell(vid)
    N_P = length(vid);
    n_B = length(vid_S) - 1;
    
    eid = cellfun(@helper, vid, 'UniformOutput', false);
    
    eid_B = zeros(2, n_B);
    for i = 1:N_P
        filt = eid{i} ~= 0;
        eid_B(:, filt) = [repmat(i, 1, sum(filt)); eid{i}(filt)];
    end
else
    eid_B = helper(vid);
end

    function eid_B = helper(vid)
        vid_ = vid([1:end; 2:end 1]');

        [~, eid_B] = ismember(vid_S_, vid_, 'rows');

        eid_B(~isB(1:end-1)) = 0;
        eid_B = eid_B';
    end

end