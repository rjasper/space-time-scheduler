function [vid_S_L, vid_S_R] = split_shared_vertices(ncut, ocuts, side, V, vid, vid_S, eid_C, isB)

n_ocuts = size(ocuts, 2);
n_V = size(V, 2);

eid_B = cellfun( ...
    @(vid_S, isB, side) determine_bridge_eid(vid, vid_S, isB, side), ...
    vid_S, isB, num2cell(side), ...
    'UniformOutput', false);
dir = determine_direction;

[vid_S_L, vid_S_R] = cellfun(@split, vid_S, 'UniformOutput', false);

for i = 1:n_ocuts
    % if there is an intersection then only one
    [eid_BC, ~, cid_BC] = intersect(eid_B{i}', eid_C', 'rows');
    
    if isempty(eid_BC)
        continue;
    end
    
    vid_BC = cid_BC + n_V;
    
    % append or prepend new shared vertex to existing ones
    switch dir(i)
        case 'l' % new cut goes to the left
            vid_S_L{i} = [vid_S_L{i} vid_BC];
            vid_S_R{i} = [vid_BC vid_S_R{i}];
        case 'r' % new cut goes to the right
            vid_S_L{i} = [vid_BC vid_S_L{i}];
            vid_S_R{i} = [vid_S_R{i} vid_BC];
    end
end

    function dir = determine_direction
        nvec = line2vec(ncut);
        ovec = line2vec(ocuts);
        
        nvec90 = vec90(nvec);
        
        h = nvec90' * ovec;
        
        dir = arrayfun(@helper, h);
        
        function dir = helper(h)
            if h < 0
                dir = 'l'; % new cut goes to the left
            elseif h > 0
                dir = 'r'; % new cut goes to the right
            else
                dir = '-'; % new cut is parallel
            end
        end
    end

    function [vid_S_L, vid_S_R] = split(vid_S)
        pts = V(:, vid_S);
        y_T = transform_y(pts, ncut);
        
        vid_S_L = vid_S(y_T >= 0); % use epsilon
        vid_S_R = vid_S(y_T <= 0);
    end

end

function y_T = transform_y(pts, cut)

n_pts = size(pts, 2);
vec = calc_line_vec(cut);
l = norm(vec);
R_y = [-vec(2) vec(1)] / l;

y_T = R_y * (pts - repmat(cut(1:2), 1, n_pts));

end