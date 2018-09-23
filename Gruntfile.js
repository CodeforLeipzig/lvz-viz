module.exports = function(grunt) {
  'use strict';
  require('load-grunt-tasks')(grunt);

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),

    gruntfile_js: 'Gruntfile.js',

    build_dir: 'build',
    build_dir_js: '<%= build_dir %>/resources/main/public/js',
    build_app_js: '<%= build_dir_js %>/app.js',
    build_app_min_js: '<%= build_dir_js %>/app.min.js',

    src_dir: 'src/main/resources/public',
    src_dir_js: '<%= src_dir %>/js',
    src_app_js: '<%= src_dir_js %>/app.js',
    src_files_js: '<%= src_dir_js %>/**/*.js',
    src_files_html: '<%= src_dir %>/**/*.html',

    jshint: {
      options: {
        boss: true,
        browser: true,
        curly: true,
        esnext: true,
        globals: {
          angular: true,
          '_': true,
          '$': true,
        },
        immed: true,
        newcap: true,
        noarg: true,
        node: true,
        sub: true,
        undef: false,
        unused: true,
        quotmark: 'single',
        validthis: true
      },
      dist: {
        src: [ '<%= gruntfile_js %>', '<%= src_files_js %>' ]
      }
    },
    concat: {
      options: {
        separator: grunt.util.linefeed
      },
      // first build without app.js
      core: {
        src: [ '<%= src_files_js %>', '!<%= src_app_js %>' ],
        dest: '<%= build_app_js %>'
      },
      // second build with prepended app.js
      dist: {
        src: [ '<%= src_app_js %>', '<%= concat.core.dest %>' ],
        dest: '<%= concat.core.dest %>'
      }
    },
    uglify: {
      options: {
        // the banner is inserted at the top of the output
        banner: '/*! <%= pkg.name %> <%= grunt.template.today("dd-mm-yyyy") %> */\n'
      },
      dist: {
        src: '<%= concat.dist.dest %>',
        dest: '<%= build_app_min_js %>'
      }
    },
    htmlangular: {
      files: {
        src: [ '<%= src_dir %>/**/*.html' ]
      },
      options: {
        reportpath: '<%= build_dir %>/html-angular-validate-report.json',
        reportCheckstylePath: null
      }
    },
    clean: {
      js: [ '<%= build_dir_js %>/*.js', '!<%= build_app_min_js %>' ]
    },
    watch: {
      scripts: {
        files: [ '<%= gruntfile_js %>', '<%= src_files_js %>' ],
        tasks: [ 'default' ]
      },
      html: {
        files: [ '<%= gruntfile_js %>', '<%= src_files_html %>' ],
        tasks: [ 'htmlangular' ]
      }
    }
  });

  // the build task for production usage without devDependencies
  grunt.registerTask('build', [ 'concat', 'uglify', 'clean' ]);
  // the default task for development usage
  grunt.registerTask('default', [ 'jshint', 'build' ]);
};
