function [V, vid, R2V_map] = merge_polygons(V_L, vid_L, vid_S_L, V_R, vid_R, vid_S_R)

n_L = size(V_L, 2);
n_R = size(V_R, 2);
n_S = length(vid_S_L); % == length(vid_S_R)

if n_S ~= length(vid_S_R)
    error('vid_S_L and vid_S_R do not have the same number of elements');
end

if n_S == 0 % == isempty(vid_S_R)
    V = [V_L V_R];
    R2V_map = n_L + (1:n_R);
    vid = [vid_L cellfun(@(v) n_L + v, vid_R, 'UniformOutput', false)];
    
    return;
end

N_L = length(vid_L);
N_R = length(vid_R);
N_P_max = N_L + N_R;
n_P_max = sum([ cellfun(@length, vid_L) cellfun(@length, vid_R) ]);
n_B = n_S - 1;

% calculate index mapping from V_R to V
non_vid_S_R = setdiff(1:n_R, vid_S_R);
R2V_map = zeros(1, n_R);
R2V_map(vid_S_R) = vid_S_L;
R2V_map(non_vid_S_R) = n_L + (1:n_R-n_S);

V = [V_L V_R(:, non_vid_S_R)];

vid = cell(1, N_P_max);

[mapTo_L, mapFrom_L] = determine_mapping(vid_L, vid_S_L, 'left');
[mapTo_R, mapFrom_R] = determine_mapping(vid_R, vid_S_R, 'right');

% merge polygons connected by bridges

unused_B = mapFrom_L(1, :) ~= 0 & mapFrom_R(1, :) ~= 0;
unused_L = true(1, N_L);
unused_R = true(1, N_R);
vid_i = zeros(1, n_P_max);
i = 1;
while any(unused_B)
    % find next unused bridge
    bid_first = find(unused_B, 1, 'first');
    bid = bid_first;

    no_loop = true;

    j = 1;
    while no_loop
        [j, bid, pid_R] = step(j, bid, vid_R, mapFrom_R, mapTo_R, 'right');
        
        unused_B(bid) = false;
        [j, bid, pid_L] = step(j, bid, vid_L, mapFrom_L, mapTo_L, 'left');
        if pid_L ~= 0
            unused_L(pid_L) = false;
        end

        % check if polygon is complete
        no_loop = bid ~= bid_first;
    end

    vid{i} = vid_i(1:j-1);
    
    i = i + 1;
end

% translate indices of remaining polygons (non-cut)

vid_L_ = vid_L(unused_L);
vid_R_ = cellfun(@(v) R2V_map(v), vid_R(unused_R), 'UniformOutput', false);

% collect all polygons (merged and non-cut polygons)
vid = [vid(1:i-1) vid_L_ vid_R_];

    % determines the from-vertex-to-bridge (mapTo) and
    % from-bridge-to-vertex (mapFrom) mapping
    function [mapTo, mapFrom] = determine_mapping(vid, vid_S, side)
        % number of polygons
        N_P = length(vid);
        
        mapFrom = zeros(2, n_B);
        % note that mapFrom will be modified by the helper's side effect
        % however, the modifications will be independent from each other
        mapTo = cellfun(@helper, vid, num2cell(1:N_P), 'UniformOutput', false);
        
        % changes mapFrom
        function mapTo = helper(vid, i)
            % number of polygon vertices
            n_P = length(vid);
            
            % find vertices on the cut
            [isS, S_idx] = ismember(vid, vid_S);
            % find bridges
            B_filt = isS & isS([2:end 1]);
            B_idx = find(B_filt);
            B_idx_ = idxmod(B_idx+1, n_P);

            % determine bridge IDs
            switch side
                case 'left'
                    B = S_idx( B_filt );
                case 'right'
                    S_idx_ = circshift(S_idx, [0 -1]);
                    B = S_idx_( B_filt );
            end
            
            mapTo = zeros(1, n_P);
            mapTo(B_filt) = B;
            
            mapFrom(1, B) = i; % polygon ID (pid)
            mapFrom(2, B) = B_idx_; % polygon vertex ID (pvid)
        end
    end

    function [j_, bid_, pid] = step(j, bid, vid, mapFrom, mapTo, side)
        unused_B(bid) = false;
        
        pid = mapFrom(1, bid);
        pvid_start = mapFrom(2, bid);
        
        % no polygon on this side of the bridge
        % save the point and go to the side
        if pid == 0
            switch side
                case 'left'
                    vid_i(j) = vid_S_L(bid+1);
                case 'right'
                    vid_i(j) = vid_S_L(bid);
            end
            
            j_ = j + 1;
            bid_ = bid;
            return;
        else
            isB = mapTo{pid} ~= 0;
            pvid_end = bnext(isB, pvid_start);

            vid_pid = vid{pid};
            n_pid = length(vid_pid);

            I = interval(pvid_start, pvid_end, n_pid);
            n_I = length(I);

            switch side
                case 'left'
                    I_ = vid_pid( I );
                    unused_L(pid) = false;
                case 'right'
                    I_ = R2V_map( vid_pid(I) );
                    unused_R(pid) = false;
            end

            vid_i(j-1 + (1:n_I)) = I_;
            
            j_ = j + n_I;
            bid_ = mapTo{pid}(pvid_end);
        end
    end
end

% gets the next true index after the given one
% is cyclic
function next = bnext(b, idx)
    b_shift = circshift(b, [0 1-idx]);

    next_shift = find(b_shift, 1, 'first');
    next = idxmod( next_shift + idx-1, length(b) );
end

% gives the interval between i_start and i_end (without i_end)
% is cyclic
function I = interval(i_start, i_end, n)
    if i_start <= i_end
        I = i_start:i_end-1;
    else
        I = [i_start:n 1:i_end-1];
    end
end