function b = visible3(p1, p2, Os)

eps = 0; % define epsilon
line = [p1; p2];

b = all( arrayfun(@helper, Os) );

    function b = helper(Os)
        % determine bridges
        % check bridge intervals
        
%         n_Os = size(Os, 2);
%         
%         Os_T = transform(Os, line);
%         
%         side = cellfun(@determine_side, Os_T, 'UniformOutput', false);
        
%         filt_lr = side ~= 'b';
%         idx_lr = find(filt_lr);
%         side_lr = side(filt_lr);
%         
%         [sidx, eidx] = regexp(side_lr([1:end 1]), '(lr)|(rl)');
%         
%         b = ~any( arrayfun(@check, idx_lr(sidx), idx_lr(eidx)) );
        
%         function b = check(sidx, eidx)
%             l = mod(eidx - sidx + 1, n_Os);
%             
%             if l == 2
%                 % determine crossing point
%                 [~, t1, t2] = line_line_intersect(line, [Os_T(:, sidx); Os_T(:, eidx)]);
%                 
%             else
%                 % determine first and last border point inbetween
%             end
%         end
    end

%     function side = determine_side(p)
%         y = p(2, :);
%         side = arrayfun(@helper, y);
% 
%         function side = helper(y)
%             if y < -eps
%                 side = 'r';
%             elseif y > eps
%                 side = 'l';
%             else
%                 side = 'b';
%             end
%         end
%     end

    end