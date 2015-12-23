function varargout = UI_main(varargin)
% UI_MAIN MATLAB code for UI_main.fig
%      UI_MAIN, by itself, creates a new UI_MAIN or raises the existing
%      singleton*.
%
%      H = UI_MAIN returns the handle to a new UI_MAIN or the handle to
%      the existing singleton*.
%
%      UI_MAIN('CALLBACK',hObject,eventData,handles,...) calls the local
%      function named CALLBACK in UI_MAIN.M with the given input arguments.
%
%      UI_MAIN('Property','Value',...) creates a new UI_MAIN or raises the
%      existing singleton*.  Starting from the left, property value pairs are
%      applied to the GUI before UI_main_OpeningFcn gets called.  An
%      unrecognized property name or invalid value makes property application
%      stop.  All inputs are passed to UI_main_OpeningFcn via varargin.
%
%      *See GUI Options on GUIDE's Tools menu.  Choose "GUI allows only one
%      instance to run (singleton)".
%
% See also: GUIDE, GUIDATA, GUIHANDLES

% Edit the above text to modify the response to help UI_main

% Last Modified by GUIDE v2.5 12-Mar-2015 18:47:40

% Begin initialization code - DO NOT EDIT
gui_Singleton = 1;
gui_State = struct('gui_Name',       mfilename, ...
                   'gui_Singleton',  gui_Singleton, ...
                   'gui_OpeningFcn', @UI_main_OpeningFcn, ...
                   'gui_OutputFcn',  @UI_main_OutputFcn, ...
                   'gui_LayoutFcn',  [] , ...
                   'gui_Callback',   []);
if nargin && ischar(varargin{1})
    gui_State.gui_Callback = str2func(varargin{1});
end

if nargout
    [varargout{1:nargout}] = gui_mainfcn(gui_State, varargin{:});
else
    gui_mainfcn(gui_State, varargin{:});
end
% End initialization code - DO NOT EDIT


% --- Executes just before UI_main is made visible.
function UI_main_OpeningFcn(hObject, eventdata, handles, varargin)
% This function has no output args, see OutputFcn.
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% varargin   command line arguments to UI_main (see VARARGIN)

% Choose default command line output for UI_main
handles.output = hObject;
handles.dirname = '';
handles.dir_avail_flag = 0;
handles.thres_stage1 = 0;
handles.thres_stage2 = 0;
handles.detect = 0;
handles.featIdx = [2:13 [2:13]+39 118:133];
handles.uname = 'Sarthak';
handles.detected_folder_path_male = strcat('C:\Users\',handles.uname,'\Dropbox\Matlab Workspace\PR Project\detections\male');

handles.detected_folder_path_female = strcat('C:\Users\',handles.uname,'\Dropbox\Matlab Workspace\PR Project\detections\female');
handles.recorded_folder_path = strcat('C:\Users\',handles.uname,'\Dropbox\Matlab Workspace\PR Project\recordings\');

addpath(genpath('C:\Users\Sarthak\Dropbox\Matlab Workspace\PR Project'));
% Update handles structure
guidata(hObject, handles);

% UIWAIT makes UI_main wait for user response (see UIRESUME)
% uiwait(handles.figure1);


% --- Outputs from this function are returned to the command line.
function varargout = UI_main_OutputFcn(hObject, eventdata, handles) 
% varargout  cell array for returning output args (see VARARGOUT);
% hObject    handle to figure
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)

% Get default command line output from handles structure
varargout{1} = handles.output;



% --- Executes on button press in select_folder.
function select_folder_Callback(hObject, eventdata, handles)
% handles.output_direc = 'C:\Users\Sarthak\Dropbox\Matlab Workspace\Trimmed_Clips-2015-03-05\Trimmed Clips\';
[handles.filename,dirname] = uigetfile('C:\Users\Sarthak\Dropbox\Matlab Workspace\PR Project\audio_data', 'Choose wave audio file ');
set(handles.text_wavename,'string',handles.filename);
% if(dirname ~= 0)
%    handles.dirname = dirname;
%    handles.dir_avail_flag = 1;
% else
%    disp('Directory not selected');
%    %handles.dir_avail_flag = 0;
% end

[handles.signal,handles.fs] = wavread([dirname,handles.filename]);
if(size(handles.signal,2)==2)
   handles.signal=mean(handles.signal,2); 
end
guidata(hObject, handles);

% --- Executes on button press in start_detection.
function start_detection_Callback(hObject, eventdata, handles)
% clear axes on the UI
handles.detect = 1;
set(handles.debug,'string',strcat('Started-',num2str(handles.detect)));

hold off;
plot(handles.axes1, 0,0);
handles.x_axis_val = 1;

% check action to perform
button_label = get(handles.start_detection, 'string');

% check errors in param setting
%check_errors(hObject,handles);
disable_all_buttons(hObject,handles);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%% Start feature extraction here %%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Find number of 2 sec samples
sig_len = length(handles.signal);
samp_len = 2*handles.fs; 
nSamples = floor(sig_len / samp_len);

if(sig_len <= samp_len)
    %%%%%%
end

samp1 = 1;
samp2 = samp_len;
sample = handles.signal(samp1: samp2);
samp1 = samp1 + samp_len;
samp2 = samp2 + samp_len;
set(handles.status, 'string','Sampling Data...');
drawnow;
for for_all_samples = 2: nSamples
    drawnow;
    if(handles.detect == 0)
        set(handles.debug, 'string',num2str(handles.detect));
        break;
        return;
    else,
        set(handles.debug, 'string','Still Running');
    end
    previous_sample = sample;
    sample = handles.signal(samp1: samp2);
    handles.sample=sample;
    % feature extraction
    set(handles.status, 'string','Extracting Features...');
    drawnow;
    [mfcc_features,labels] = extract_features(sample,handles.fs,'');
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
               %CLASSIFICATION%
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    load('differentsvm.mat');
    decision=svmpredict(double(0),double(mfcc_features(1,:)),svmlinearboth);
    set(handles.status, 'string','Deciding...');
    drawnow;
    if( decision==1 )
        wav_out_path = strcat(handles.detected_folder_path_female, handles.filename,num2str(for_all_samples),'.wav');
        wavwrite(sample, handles.fs, wav_out_path);
        set(handles.text_decision, 'string','FEMALE!');
        %set(handles.text17,'string',num2str(demoPitch(sample,handles.fs)));
        res_combined = 1;
        drawnow;
    else
        wav_out_path = strcat(handles.detected_folder_path_male, handles.filename,num2str(for_all_samples),'.wav');
        wavwrite(sample, handles.fs, wav_out_path);
        res_combined = 0;
        set(handles.text_decision, 'string','MALE!');
        %set(handles.text17,'string',num2str(demoPitch(sample,handles.fs)));
        drawnow;
    end
    [hObject, handles] = plot_results(hObject,handles,res_combined);
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %%%% Plotting of detections
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
     pause(0.1);
    
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

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

%end
enable_all_buttons(hObject,handles);
guidata(hObject, handles);

function [hObject, handles] = check_errors(hObject, handles)
if(handles.dir_avail_flag ~= 1)
    title = 'Error in finding directory name';
    msg = 'No directory selected';
    pop_up_dialog(title, msg);
end
guidata(hObject, handles);
disp('Error Check done !');

function [hObject, handles] = disable_all_buttons(hObject, handles)
set(handles.select_folder, 'enable','off');
set(handles.btn_live, 'enable','off');
set(handles.start_detection, 'enable','off');
guidata(hObject, handles);


function [hObject, handles] = enable_all_buttons(hObject, handles)
set(handles.select_folder, 'enable','on');
set(handles.btn_live, 'string','Start Live Session');
set(handles.start_detection,  'enable','on');
set(handles.btn_live,  'enable','on');
guidata(hObject, handles);

function [hObject, handles] = plot_results(hObject,handles,result)
x_val = handles.x_axis_val;
v_val = x_val + size(handles.sample,1);
xaxis=[x_val:v_val-1];
tobeplot=handles.sample;
hold on;
if(result == 1)
    plot(xaxis, tobeplot' , 'r-');
else
    plot(xaxis, tobeplot' , 'g-');
end
drawnow;
handles.x_axis_val = v_val;
guidata(hObject, handles);


function [hObject, handles] = pop_up_dialog(hObject, handles, title, msg)
guidata(hObject, handles);
disp('pop up');

% --- Executes on button press in realTime.
function realTime_Callback(hObject, eventdata, handles)
% hObject    handle to realTime (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
recObj = audiorecorder;

% --- Executes on button press in btn_live.
function btn_live_Callback(hObject, eventdata, handles)
% hObject    handle to btn_live (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
% clear axes on the UI
hold off;
plot(handles.axes1, 0,0);
handles.x_axis_val = 0;
handles.running=1;
% check action to perform
button_label = get(handles.start_detection, 'string');
handles.detect = 1;


% check errors in param setting
%check_errors(hObject,handles);
disable_all_buttons(hObject,handles);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%% Start feature extraction here %%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
count=0;
flag=1;
while count<10,
    count=count+1;
    set(handles.debug,'string',num2str(count));

    AR = dsp.AudioRecorder('OutputNumOverrunSamples',true);
    wav_out_path = strcat(handles.recorded_folder_path,'recording',num2str(count),'.wav');
    AFW = dsp.AudioFileWriter(wav_out_path,'FileFormat', 'WAV');
    set(handles.status, 'string','Speak into microphone now');
    drawnow;
    tic;
    while toc < 2,
      [audioIn,nOverrun] = step(AR);
      step(AFW,audioIn);
      if nOverrun > 0
        fprintf('Audio recorder queue was overrun by %d samples\n'...
            ,nOverrun);
      end
    end
    release(AR);
    release(AFW);
    set(handles.status, 'string','Recording complete'); 
    drawnow;
    [handles.signal,handles.fs] = wavread(wav_out_path);
    if(size(handles.signal,2)==2)
       handles.signal=mean(handles.signal,2); 
    end
    if(flag==1),
        sample=handles.signal;
        previous_sample=sample;
        flag=0;
        continue;
    else
        previous_sample=sample;
        sample=handles.signal;
    end
    handles.sample=sample;
    % feature extraction
    set(handles.status, 'string','Extracting Features...');
    drawnow;
    [mfcc_features,labels] = extract_features(sample,handles.fs,'');
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                %CLASSIFICATION%
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    load('differentsvm.mat');
    decision=svmpredict(double(0),double(mfcc_features(1,:)),svmrbfboth);
    dp=demoPitch(sample,handles.fs);
    if(dp>150),
        decision=1;
    else
        decision=0;
    end
    set(handles.status, 'string','Deciding...');
    drawnow;
    if( decision==1 )
        wav_out_path = strcat(handles.detected_folder_path_female, 'recording',num2str(count),'.wav');
        wavwrite(sample, handles.fs, wav_out_path);
        set(handles.text_decision, 'string','FEMALE!');
        %set(handles.text17,'string',num2str(demoPitch(sample,handles.fs)));
        res_combined=1;
        drawnow;
    else
        wav_out_path = strcat(handles.detected_folder_path_male, 'recording',num2str(count),'.wav');
        wavwrite(sample, handles.fs, wav_out_path);
        res_combined = 0;
        set(handles.text_decision, 'string','MALE!');
        %set(handles.text17,'string',num2str(demoPitch(sample,handles.fs)));
        drawnow;
    end
    [hObject, handles] = plot_results(hObject,handles,res_combined);
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    %%%% Plotting of detections
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

end


% --- Executes on button press in reset.
function reset_Callback(hObject, eventdata, handles)
% hObject    handle to reset (see GCBO)
% eventdata  reserved - to be defined in a future version of MATLAB
% handles    structure with handles and user data (see GUIDATA)
set(handles.select_folder, 'enable','on');
set(handles.start_detection, 'string','Start Detection From File');
set(handles.btn_live, 'enable','on');
set(handles.start_detection,  'enable','on');
set(handles.text_decision,  'string','-');
set(handles.status,  'string','-');
set(handles.text_wavename,'string','-');
set(handles.debug,'string','-');
guidata(hObject, handles);

