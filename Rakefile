# encoding: utf-8

require 'ant'

namespace :build do
  source_dir = 'ext/src'
  build_dir = 'ext/build'
  ruby_dir = 'lib'

  task :setup do
    mkdir_p build_dir
    ant.path :id => 'compile.class.path' do
      pathelement :location => File.join(ENV['MY_RUBY_HOME'], 'lib', 'jruby.jar')
      pathelement :location => File.join(ENV['HADOOP_RELEASE'], 'hadoop-core-0.20.203.0.jar')
#      File.foreach(File.expand_path('../.classpath', __FILE__)) do |path|
#        pathelement :location => path.chop!
#      end
    end
  end

  task :compile => :setup do
    ant.javac :destdir => build_dir, :includeantruntime => 'no', :target => '1.6', :source => '1.6', :debug => 'on' do
      classpath :refid => 'compile.class.path'
      src { pathelement :location => source_dir }
    end
  end

  task :jars => :compile do
    ant.jar :destfile => 'lib/rubydoop.jar', :basedir => build_dir 
  end

  task :clean do
    rm_rf build_dir
    rm Dir['lib/rubydoop*.jar']
  end
end

desc 'Build the lib/rubydoop.jar'
task :build => 'build:jars'

namespace :setup do
  task :hadoop do
    hadoop_release = ENV['HADOOP_RELEASE'] || 'hadoop-1.0.3/hadoop-1.0.3-bin'
    hadoop_url = "http://archive.apache.org/dist/hadoop/common/#{hadoop_release}.tar.gz"
    FileUtils.mkdir_p('tmp')
    Dir.chdir('tmp') do
      command = (<<-END).lines.map(&:strip).join(' && ')
      rm -fr hadoop*
      curl --progress-bar -O '#{hadoop_url}'
      tar xf hadoop*.tar.gz
      END
      system(command)
    end
  end

  task :test_project do
    Dir.chdir('spec/integration/test_project') do
      command = (<<-END).lines.map(&:strip).join(' && ') 
      rvm gemset create rubydoop-test_project
      rvm $RUBY_VERSION@rubydoop-test_project do bundle install
      END
      puts command
      Bundler.clean_system(command)
    end
  end

  task :classpath do
    File.open('.classpath', 'w') do |io|
      hadoop_home = File.expand_path(Dir["tmp/hadoop*"].first)
      %x(#{hadoop_home}/bin/hadoop classpath).chomp.split(':').each do |pattern|
        Dir[pattern].each do |path|
          io.puts(path)
        end
      end
    end
  end
end

desc 'Download Hadoop and set up classpath'
task :setup => ['setup:hadoop', 'setup:test_project', 'setup:classpath']

require 'rspec/core/rake_task'

RSpec::Core::RakeTask.new(:spec) do |r|
  r.rspec_opts = '--tty'
end

task :spec => :build

require 'bundler'

namespace :gem do
  Bundler::GemHelper.install_tasks
end

desc 'Release a new gem version'
task :release => [:spec, 'gem:release']
