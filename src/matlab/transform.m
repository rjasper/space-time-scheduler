function p_ = transform(p, line)

if iscell(p)
    p_ = transform(@helper, p, 'UniformOutput', false);
else
    p_ = helper(p);
end

    function p_ = helper(p)
        n_p = size(p, 2);
        L1 = line(1:2);
        dL = line2vec(line);

        l_length = sqrt( sum(dL.^2) );

        % rotation matrix
        R = [dL(1) dL(2); -dL(2) dL(1)] / l_length;
        % transform P and l
        p_ = R * (p - repmat(L1, 1, n_p)); 
    end

end