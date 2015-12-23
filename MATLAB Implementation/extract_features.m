function [feature_vector,labels] = extract_features(audio,fs,audioname)

audio(audio == 0) = [];
if(length(audio)<0.3*fs)
    feature_vector = [];
    labels = [];
   return; 
end

[ar,ac] = size(audio);
if(ar<ac)
    audio = audio';
end

win = 0.050;
step = 0.050;
    

% convert to mono, if stereo
if(size(audio,2) == 2)
    audio = sum(audio,2);
end

% Find number of 2 sec samples
sig_len = length(audio);
samp_len = 2*fs; 
nSamples = floor(sig_len / samp_len);

% initialize variables
feature_vector = [];
labels = [];
feat_count = 1;

if(sig_len <= samp_len) 
    
    [segments, fs] = detectVoiced(audio,fs);
    if(isempty(segments))
        feature_vector = [];
        labels = [];
        return;
    end
    segSig = [];
    for i = 1: length(segments)
        segSig = [segSig; segments{i}];
    end
    if(size(segSig,2)~=1)
        disp(size(audio)); 
        disp(size(segSig)); 
        disp(size(segments));
        error('segSig error');
    end
    audio = segSig;
    sample=audio;
    mfc_vec = feature_extr_mfcc(sample,fs);
    energy_vec = feature_energy(sample);
    pitch_vec=demoPitch(sample,fs);
    shorttimeenergy_vec = ShortTimeEnergy(sample, floor(win*fs), floor(step*fs));
    dft=getDFT(sample,fs);
    spectralcentroid_vec = feature_spectral_centroid(dft,fs);
    zcr_vec = feature_zcr(sample);
    energyentropy_vec = feature_spectral_entropy(dft, floor(win*fs));
    feature_vector(feat_count, :) = [mfc_vec energy_vec mean(shorttimeenergy_vec') mean(spectralcentroid_vec') zcr_vec energyentropy_vec pitch_vec];
    
    %feature_vector(feat_count, :) = [hog];
    labels{feat_count} = strcat(audioname);
    return;
end

samp1 = 1;
samp2 = samp_len;
for for_all_samples = 1: nSamples
    sample = audio(samp1: samp2);
    
    [segments, fs] = detectVoiced(sample,fs);
    if(isempty(segments))
        samp1 = samp1 + samp_len;
        samp2 = samp2 + samp_len;
        if( samp2 > sig_len )
            if( samp1 > (sig_len-samp_len/2) )
                break;
            else
                samp2 = sig_len;
            end
        end
        continue;
    end
    segSig = [];
    for i = 1: length(segments)
        segSig = [segSig; segments{i}];
    end
    if(size(segSig,2)~=1)
        disp(size(segSig)); 
        disp(size(segments));
        error('segSig error');
    end
    sample = segSig;
    mfc_vec = feature_extr_mfcc(sample,fs);
    energy_vec = feature_energy(sample);
    shorttimeenergy_vec = ShortTimeEnergy(sample, floor(win*fs), floor(step*fs));
    dft=getDFT(sample,fs);
    pitch_vec=demoPitch(sample,fs);
    spectralcentroid_vec = feature_spectral_centroid(dft,fs);
    zcr_vec = feature_zcr(sample);
    energyentropy_vec = feature_spectral_entropy(dft, floor(win*fs));
    feature_vector(feat_count, :) = [mfc_vec energy_vec mean(shorttimeenergy_vec') mean(spectralcentroid_vec') zcr_vec energyentropy_vec pitch_vec];
    %feature_vector(feat_count, :) = [hog];
    labels{feat_count} = strcat(audioname,'_',num2str(for_all_samples));   
    feat_count = feat_count + 1;
    
    samp1 = samp1 + samp_len;
    samp2 = samp2 + samp_len;
    if( samp2 > sig_len )
        if( samp1 > (sig_len-samp_len/2) )
            break;
        else
            samp2 = sig_len;
        end
    end    
end
